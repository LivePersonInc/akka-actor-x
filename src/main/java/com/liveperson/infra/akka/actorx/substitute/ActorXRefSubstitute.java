package com.liveperson.infra.akka.actorx.substitute;

import akka.actor.ActorRef;
import com.liveperson.infra.akka.actorx.ActorXDirector;
import com.liveperson.infra.akka.actorx.ActorXDirectorOffice;
import com.liveperson.infra.akka.actorx.extension.ActorXConfig;
import com.liveperson.infra.akka.actorx.staff.AssistantUtils;
import com.liveperson.infra.akka.actorx.staff.StaffMessage;
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

    @Pointcut("execution(* akka.actor.ActorRef.tell(..))")
    public void tellPC() {}

    @Around("tellPC()")
    public Object aroundTell(ProceedingJoinPoint pjp) throws Throwable {

        ActorRef actorRef = (ActorRef)pjp.getTarget();
        Object[] arguments = pjp.getArgs();
        Object msg = arguments[0];
        ActorRef sender = (arguments.length > 1) ? (ActorRef)arguments[1] : null;

        // Trace Logging
        String actorRefPath = (actorRef == null) ? "null" : actorRef.path().name();
        String senderPath = (sender == null) ? "null" : sender.path().name();
        String msgClassName = (msg == null) ? "null" : msg.getClass().getName();
        logger.trace("[{}] before tell(msg # sender) : {} # {}", actorRefPath, msgClassName, senderPath);

        // If is internal staff messages then DO NOT delegate back into the ActorXDirector
        // This can cause an endless loop
        if (AssistantUtils.isInternalStaffMessage(msg)) {
            return pjp.proceed();
        }

        try {

            // Before
            Object wrappedMessage = msg;
            ActorXDirector actorXDirector = null;

            actorXDirector = ActorXDirectorOffice.getActorXDirector();
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
            logger.trace("[{}] after tell(msg # sender) : {} # {}", actorRefPath, msgClassName, senderPath);
        }
    }
}
