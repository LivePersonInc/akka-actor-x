package com.liveperson.interaction.infra.akka.actorx.substitute;

import akka.actor.ActorRef;
import akka.pattern.PromiseActorRef;
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
public class ActorXRefSubstitute {

    private Logger logger = LoggerFactory.getLogger(ActorXRefSubstitute.class);

    // TODO Change trace logging to configurable parameter (do not rely on logger log level)
    // TODO Change trace logging to configurable parameter (do not rely on logger log level)
    // TODO Change trace logging to configurable parameter (do not rely on logger log level)

    /*@Pointcut("execution(* akka.actor.ActorRef.tell(..)) && this(actorRef) && args(msg, sender)")
    public void tellPC(ActorRef actorRef, Object msg, ActorRef sender) {}*/

    @Pointcut("execution(* akka.actor.ActorRef.tell(..))")
    public void tellPC() {}

    /*@Around("tellPC(actorRef, msg, sender)")
    public Object aroundTell(ProceedingJoinPoint pjp, ActorRef actorRef, Object msg, ActorRef sender) throws Throwable {*/

    @Around("tellPC()")
    public Object aroundTell(ProceedingJoinPoint pjp) throws Throwable {

        ActorRef actorRef = (ActorRef)pjp.getTarget();
        Object[] arguments = pjp.getArgs();
        Object msg = arguments[0];
        ActorRef sender = (arguments.length > 1) ? (ActorRef)arguments[1] : null;

        /*// TODO REMOVE
        logger.trace(">>> ACTOR-REF >>> TARGET CLASS = {}", (actorRef == null) ? "null" : actorRef.getClass().getName());
        logger.trace(">>> ACTOR-REF >>> THIS CLASS = {}", (pjp.getThis() == null) ? "null" : pjp.getThis().getClass().getName());*/

        String actorRefPath = (actorRef == null) ? "null" : actorRef.path().name();
        String senderPath = (sender == null) ? "null" : sender.path().name();
        String msgClassName = (msg == null) ? "null" : msg.getClass().getName();
        logger.trace(">>> ACTOR-REF [{}] >>> before tell(msg # sender) : {} # {}", actorRefPath, msgClassName, senderPath);

        try {

            // Before
            Object wrappedMessage = msg;
            ActorXDirector actorXDirector = ActorXDressingRoom.getActorXDirector();
            if (actorXDirector != null) {
                wrappedMessage = actorXDirector.beforeSend(actorRef, msg, sender);
            }

            // Proceed
            Object[] args = {wrappedMessage, sender};
            Object result = pjp.proceed(args);

            // After
            if (actorXDirector != null) {
                actorXDirector.afterSend(actorRef, wrappedMessage, sender);
            }

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
            logger.trace("<<< ACTOR-REF [{}] <<< after tell(msg # sender) : {} # {}", actorRefPath, msgClassName, senderPath);
        }
    }
}
