package com.liveperson.interaction.infra.akka.actorx.substitute;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.liveperson.interaction.infra.akka.actorx.ActorXDirector;
import com.liveperson.interaction.infra.akka.actorx.ActorXDressingRoom;
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
public class ActorXContextSubstitute {

    private Logger logger = LoggerFactory.getLogger(ActorXContextSubstitute.class);

    // TODO Change trace logging to configurable parameter (do not rely on logger log level)
    // TODO Change trace logging to configurable parameter (do not rely on logger log level)
    // TODO Change trace logging to configurable parameter (do not rely on logger log level)

    @Pointcut("within(com.liveperson..*)")
    public void allLpClasses() {}

    @Pointcut("execution(* akka.actor.ActorContext.actorOf(..))")
    public void actorOfPC() {}

    @Around("actorOfPC()")
    public Object aroundActorOf2(ProceedingJoinPoint pjp) throws Throwable {

        ActorContext context = (ActorContext)pjp.getTarget();
        Object[] arguments = pjp.getArgs();
        Props props = (arguments.length > 0) ? (Props)arguments[0] : null;
        String name = (arguments.length > 1) ? (String)arguments[1] : null;

        String propsActorClassName = (props != null && props.actorClass() != null) ? props.actorClass().getName() : "null";
        logger.trace(">>> CONTEXT >>> before actorOf(propsActor # name) : {} # {}", propsActorClassName, name);

        try {

            // Before
            ActorXDirector actorXDirector = ActorXDressingRoom.getActorXDirector();
            if (actorXDirector != null) {
                actorXDirector.beforeActorOf(props, name);
            }

            // Proceed
            ActorRef actorRef = (ActorRef)pjp.proceed(/*args*/);

            // After
            if (actorXDirector != null) {
                actorXDirector.afterActorOf(actorRef);
            }

            // Result
            return actorRef;
        }
        catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
        catch (Exception e) {
            logger.warn("unexpected invocation exception: " + e.getMessage());
            throw e;
        }
        finally {
            logger.trace("<<< CONTEXT <<< after actorOf(propsActor # name) : {} # {}", propsActorClassName, name);
        }
    }
}
