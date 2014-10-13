package com.liveperson.interaction.infra.akka.actorx.role;

import akka.actor.Actor;
import akka.actor.ActorRef;
import com.liveperson.interaction.infra.akka.actorx.ActorXManuscript;
import com.liveperson.interaction.infra.akka.actorx.header.Manuscript;
import com.liveperson.interaction.infra.akka.actorx.header.MessageTrailHeader;
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
    private static final short MAX_TRAIL_SIZE = 20; // TODO configurable

    // TODO Use better data structure (keep from inside jdk?)
    private LinkedList<MessageTrailHeader.Trail> messageTrail;

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
        if (this.messageTrail.size() > MAX_TRAIL_SIZE) {
            this.messageTrail.removeLast();
        }

        // TODO Only if some configuration flag is open then print the following
        logger.trace(MessageTrailHeader.getMessageTrailString(this.messageTrail));
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
