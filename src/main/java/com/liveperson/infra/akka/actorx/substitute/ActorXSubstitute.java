package com.liveperson.infra.akka.actorx.substitute;

import akka.actor.*;
import com.liveperson.infra.akka.actorx.ActorXDirector;
import com.liveperson.infra.akka.actorx.ActorXDirectorOffice;
import com.liveperson.infra.akka.actorx.ActorXManuscript;
import com.liveperson.infra.akka.actorx.extension.ActorXConfig;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareMixin;
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

    private static Logger logger = LoggerFactory.getLogger(ActorXSubstitute.class);

    // TODO Keep around receive message (last received message)?
    // TODO Might help in test interrogation....

    @Pointcut("execution(* akka.actor.Actor.aroundReceive(..))")
    public void aroundReceivePC() {}

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


        // If actor should not be enhanced then the message should also be unwrapped before proceeding
        if (!(ActorXConfig.included(targetClass, ActorXConfig.getEnhancedPackagesInclude(), ActorXConfig.getEnhancedPackagesExclude()))) {

                if (msg != null && msg instanceof ActorXManuscript) {

                    // Trace Logging
                    logger.trace("Target actor is not in actor-x scope, un-wrapping manuscript and sending original message [{}]", targetClass);

                    // Unwrap
                    Object[] args = new Object[] {arguments[0], ((ActorXManuscript)msg).getMessage()};
                    return pjp.proceed(args);
                }
                else {
                    return pjp.proceed();
                }
        }


        // Trace Logging
        logger.trace("[{}] before aroundReceive(msg) : {}", actorName, msg);


        ActorXDirector actorXDirector = null;
        try {

            // Setup actor x
            // Get director from actor.context, or create it if does not exist
            ActorContext context = actor.context();
            ActorXHiddenRoom actorXHiddenRoom = (ActorXHiddenRoom)context;
            actorXDirector = actorXHiddenRoom.getActorXDirector();
            if (actorXDirector == null) {
                actorXDirector = new ActorXDirector(actor);
                actorXHiddenRoom.setActorXDirector(actorXDirector);
            }
            actorXDirector.setup(); // DO NOT FORGET CLEAN
            ActorXDirectorOffice.setActorXDirector(actorXDirector);

            // Before
            Object unwrappedMessage = actorXDirector.beforeReceive(msg);

            // proceed
            Object[] args = {arguments[0], unwrappedMessage};
            Object result = pjp.proceed(args);

            // TODO In general, should after be here or in finally?
            // TODO             should there be an afterReceiveWithException instead?
            // After
            actorXDirector.afterReceive(unwrappedMessage);

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

            // Clean actor x
            ActorXDirectorOffice.removeActorXDirector();
            if (actorXDirector != null) {
                actorXDirector.clean(); // DO NOT FORGET SETUP
            }

            // Trace Logging
            logger.trace("[{}] after aroundReceive(msg) : {}", actorName, msg);
        }
    }



    @DeclareMixin("akka.actor.ActorCell")
    public static ActorXHiddenRoom actorXHiddenRoomMixin(Object actorCell) {
        tracePrintCreation(actorCell);
        return new ActorXHiddenRoomImpl();
    }

    // Trace logging for debugging
    private static void tracePrintCreation(Object actorCell) {
        if (logger.isTraceEnabled()) {
            if (actorCell != null && actorCell instanceof ActorCell) {
                String cellPath = null;
                ActorCell cell = (ActorCell) actorCell;
                InternalActorRef self = cell.self();
                if (self != null && self.path() != null) {
                    cellPath = self.path().name();
                }
                logger.trace("[{}] creating actor-x hidden room", cellPath);
            }
        }
    }

    public interface ActorXHiddenRoom {
        ActorXDirector getActorXDirector();
        void setActorXDirector(ActorXDirector actorXDirector);
    }

    public static class ActorXHiddenRoomImpl implements ActorXHiddenRoom {
        private ActorXDirector actorXDirector;

        @Override
        public ActorXDirector getActorXDirector() {
            return this.actorXDirector;
        }

        @Override
        public void setActorXDirector(ActorXDirector actorXDirector) {
            this.actorXDirector = actorXDirector;
        }
    }


}
