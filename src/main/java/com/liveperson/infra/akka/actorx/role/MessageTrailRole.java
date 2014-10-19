package com.liveperson.infra.akka.actorx.role;

import akka.actor.Actor;
import akka.actor.ActorRef;
import com.liveperson.infra.akka.actorx.ActorXManuscript;
import com.liveperson.infra.akka.actorx.extension.ActorXConfig;
import com.liveperson.infra.akka.actorx.header.Manuscript;
import com.liveperson.infra.akka.actorx.header.MessageTrailHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Amit Tal
 * @since 9/21/2014
 */
public class MessageTrailRole implements Role {

    private Logger logger = LoggerFactory.getLogger(MessageTrailRole.class);

    // TODO HOW TO DEAL WITH IMMUTABLE/MUTABLE (copy messageTrail?)

    // TODO Use better data structure (keep from inside jdk?)
    private LinkedList<MessageTrailHeader.Trail> messageTrail;
    private String actorPackage;

    public MessageTrailRole(String actorPackage) {
        this.actorPackage = actorPackage;
    }

    @Override
    public void beforeReceive(ActorRef from, Object message, Actor self) {
        this.messageTrail = new LinkedList<>();
        if (message instanceof Manuscript) {
            List<MessageTrailHeader.Trail> origTrail = MessageTrailHeader.getHeader((Manuscript) message);
            if (origTrail != null) {
                this.messageTrail = new LinkedList(origTrail);
            }
        }

        String fromPath = (from == null) ? "???" : from.path().name();
        MessageTrailHeader.Trail current = null;
        if (message instanceof ActorXManuscript) {
            current = new MessageTrailHeader.Trail(self.self().path().name(), ((ActorXManuscript)message).getMessage().getClass(), fromPath);
        }
        else {
            current = new MessageTrailHeader.Trail(self.self().path().name(), message.getClass(), fromPath);
        }

        // Add to trail
        this.messageTrail.addFirst(current);
        if (this.messageTrail.size() > ActorXConfig.getRoleMessageTrailMaxHistory()) {
            this.messageTrail.removeLast();
        }

        // Trace logging
        if (logger.isTraceEnabled() &&
            ActorXConfig.isRoleMessageTrailTraceLogging()) {

            // Trace log only if
            // 1. actor class package is included
            // 2. message class package is included
            Object messageToQuery = (message instanceof ActorXManuscript) ? ((ActorXManuscript)message).getMessage() : message;
            String messageClass = messageToQuery.getClass().getName();
            if (ActorXConfig.included(this.actorPackage, ActorXConfig.getRoleMessageTrailPackagesInclude(), ActorXConfig.getRoleMessageTrailPackagesExclude()) &&
                ActorXConfig.included(messageClass, ActorXConfig.getRoleMessageTrailMessagesInclude() ,ActorXConfig.getRoleMessageTrailMessagesExclude())) {

                // Trace log
                logger.trace(MessageTrailHeader.getMessageTrailString(this.messageTrail));
            }
        }
    }

    @Override
    public void beforeSend(ActorRef to, Object message, ActorRef from) {
        if (this.messageTrail != null && message instanceof Manuscript) {
            MessageTrailHeader.setHeader((Manuscript) message, this.messageTrail);
        }
    }

    public LinkedList<MessageTrailHeader.Trail> getMessageTrail() {
        return messageTrail;
    }
}
