package com.liveperson.infra.akka.actorx.substitute;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.liveperson.infra.akka.actorx.ActorXDirector;
import com.liveperson.infra.akka.actorx.ActorXDirectorOffice;
import com.liveperson.infra.akka.actorx.extension.ActorXConfig;
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

    @Pointcut("execution(* akka.actor.ActorContext.actorOf(..))")
    public void actorOfPC() {}

    @Around("actorOfPC()")
    public Object aroundActorOf2(ProceedingJoinPoint pjp) throws Throwable {

        ActorContext context = (ActorContext)pjp.getTarget();
        Object[] arguments = pjp.getArgs();
        Props props = (arguments.length > 0) ? (Props)arguments[0] : null;
        String name = (arguments.length > 1) ? (String)arguments[1] : null;

        // Trace Logging
        String propsActorClassName = (props != null && props.actorClass() != null) ? props.actorClass().getName() : null;
        logger.trace("Before actorOf(propsActor # name) : {} # {}", propsActorClassName, name);

        try {

            // Before
            ActorXDirector actorXDirector = ActorXDirectorOffice.getActorXDirector();
            if (actorXDirector != null) {
                actorXDirector.beforeActorOf(props, name);
            }

            // Proceed
            ActorRef actorRef = (ActorRef)pjp.proceed();

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
            logger.trace("After actorOf(propsActor # name) : {} # {}", propsActorClassName, name);
        }
    }
}
