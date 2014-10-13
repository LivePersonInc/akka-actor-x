package com.liveperson.interaction.infra.akka.actorx.substitute;

import akka.actor.Actor;
import akka.actor.ActorRef;
import com.liveperson.interaction.infra.akka.actorx.ActorXDirector;
import com.liveperson.interaction.infra.akka.actorx.ActorXDressingRoom;
import com.liveperson.interaction.infra.akka.actorx.ActorXManuscript;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Amit Tal
 * @since 10/4/2014
 */
@Aspect
public class ActorXSubstitute {

    private Logger logger = LoggerFactory.getLogger(ActorXSubstitute.class);

    // TODO Change trace logging to configurable parameter (do not rely on logger log level)
    // TODO Change trace logging to configurable parameter (do not rely on logger log level)
    // TODO Change trace logging to configurable parameter (do not rely on logger log level)


    // TODO Keep alive instance of actor x on this instance?
    // TODO Instead of creating each time (performance)

    // TODO Keep around receive message (last received message)?
    // TODO Might help in test interrogation....

    @Pointcut("within(com.liveperson..*)")
    public void allLpClasses() {}

    @Pointcut("execution(* akka.actor.Actor.aroundReceive(..))")
    public void aroundReceivePC() {}

    /*@Pointcut("execution(* akka.actor.Actor.aroundReceive(..)) && this(actor) && args(receive, msg)")
    public void aroundReceivePC(Actor actor, PartialFunction<Object, BoxedUnit> receive, Object msg) {}*/


    /*@Around("aroundReceivePC(actor, receive, msg)")
    public Object doAround(ProceedingJoinPoint pjp, Actor actor, PartialFunction<Object, BoxedUnit> receive, Object msg) throws Throwable {*/

    @Around("aroundReceivePC()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {

        // Should not happen, just safety check
        if (pjp.getTarget() == null || !(pjp.getTarget() instanceof Actor)) {
            return pjp.proceed();
        }

        // Extract arguments
        Actor actor = (Actor)pjp.getTarget();
        String targetClass = actor.getClass().getName();
        ActorRef actorSelf = actor.self();
        String actorName = (actorSelf != null) ? actorSelf.path().name() : null;
        Object[] arguments = pjp.getArgs();
        Object msg = (arguments.length > 1) ? arguments[1] : null;

        /*// TODO REMOVE
        logger.trace(">>> ACTOR >>> TARGET CLASS = {}", targetClass);
        logger.trace(">>> ACTOR >>> THIS CLASS = {}", (pjp.getThis() == null) ? "null" : pjp.getThis().getClass().getName());*/

        // TODO Configurable
        // TODO Define packages that should be wrapped
        // TODO ??? Is this actually all actor class that are NOT withing akka core ???
        if (!(targetClass.startsWith("com.liveperson"))) {

                if (msg != null && msg instanceof ActorXManuscript) {
                    logger.trace(">>> ACTOR >>> Target actor is not in actor-x scope, un-wrapping manuscript and sending original message [{}]", targetClass);
                    Object[] args = new Object[] {arguments[0], ((ActorXManuscript)msg).getMessage()};
                    return pjp.proceed(args);
                }
                else {
                    return pjp.proceed();
                }
        }

        logger.trace(">>> ACTOR [{}] >>> before aroundReceive(msg) : {}", actorName, msg);
        try {

            // Setup actor x
            ActorXDressingRoom.setActorXDirector(new ActorXDirector(actor));
            ActorXDirector actorXDirector = ActorXDressingRoom.getActorXDirector();

            // Before
            Object unwrappedMessage = actorXDirector.beforeReceive(msg);

            // proceed
            Object[] args = {arguments[0], unwrappedMessage};
            Object result = pjp.proceed(args);

            // After
            actorXDirector.afterReceive(unwrappedMessage);

            // Clean actor x
            ActorXDressingRoom.removeActorXDirector();
            actorXDirector.clean();

            // Result
            return result;
        }
        catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
        catch (Exception e) {
            logger.warn("unexpected invocation exception: " + e.getMessage());
            throw e;
        }
        finally {
            logger.trace("<<< ACTOR [{}] <<< after aroundReceive(msg) : {}", actorName, msg);
        }
    }

}
