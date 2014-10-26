package com.liveperson.infra.akka.actorx.role;

import akka.actor.Actor;
import akka.actor.ActorRef;
import com.liveperson.infra.akka.actorx.ActorXManuscript;
import com.liveperson.infra.akka.actorx.staff.CastTraceAssistant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Amit Tal
 * @since 10/20/2014
 */
public class CastTraceRole implements Role {

    private Logger logger = LoggerFactory.getLogger(CastTraceRole.class);

    private ActorRef selfRef;
    private String enhancedActorClassName;
    private ActorRef castTraceAssistant;
    private Set<CastTraceAssistant.Scene> sentScenes; // TODO Performance tweak, but needs memory persistency cross multiple receive
    private boolean receiveSceneSent;

    public CastTraceRole(ActorRef selfRef, String enhancedActorClassName, ActorRef castTraceAssistant) {
        this.selfRef = selfRef;
        this.enhancedActorClassName = enhancedActorClassName;
        this.castTraceAssistant = castTraceAssistant;
        this.sentScenes = new HashSet<>();
        if (this.castTraceAssistant == null) {
            logger.warn("Cannot log cast network, cast trace assistant actor not provided");
        }
    }

    @Override
    public void beforeReceive(ActorRef from, Object message, Actor self) {
        // reset flag
        this.receiveSceneSent = false;
    }

    @Override
    public void afterSend(ActorRef to, Object message, ActorRef from) {
        Object messageToQuery = (message != null && message instanceof ActorXManuscript) ? ((ActorXManuscript)message).getMessage() : message;
        String messageClassName = (messageToQuery != null) ? messageToQuery.getClass().getName() : null;
        CastTraceAssistant.Scene scene = new CastTraceAssistant.Scene(enhancedActorClassName, selfRef, messageClassName, to);
        sendScene(scene);
    }

    @Override
    public void afterActorOf(ActorRef actorRef) {
        CastTraceAssistant.Scene scene = new CastTraceAssistant.Scene(enhancedActorClassName, selfRef, actorRef);
        sendScene(scene);
    }

    @Override
    public void afterReceive(ActorRef from, Object message, Actor self) {
        if (!this.receiveSceneSent) {
            // No actor was created and no message was sent
            // This helps the Cast Trace Assistant to build a map of actor-ref to FQCN
            CastTraceAssistant.Scene scene = new CastTraceAssistant.Scene(enhancedActorClassName, selfRef);
        }
    }

    private void sendScene(CastTraceAssistant.Scene scene) {
        if (castTraceAssistant != null) {
            if (!(sentScenes.contains(scene))) {
                castTraceAssistant.tell(scene, selfRef);
            }
            this.receiveSceneSent = true;
        }
    }
}
