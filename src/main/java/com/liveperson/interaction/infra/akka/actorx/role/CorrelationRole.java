package com.liveperson.interaction.infra.akka.actorx.role;

import akka.actor.Actor;
import akka.actor.ActorRef;
import com.liveperson.interaction.infra.akka.actorx.header.CorrelationHeader;
import com.liveperson.interaction.infra.akka.actorx.header.Manuscript;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Amit Tal
 * @since 9/21/2014
 */
public class CorrelationRole implements Role {

    private HashMap<String, String> correlationIds;

    // TODO Should correlation MDC be here?
    // TODO Can use optional? (see correlationsExists)

    @Override
    public void beforeReceive(ActorRef from, Object message, Actor self) {
        if (message instanceof Manuscript) {
            this.correlationIds = CorrelationHeader.getHeader((Manuscript) message);

            // Add correlation ids
            if (correlationsExists()) {
                for (Map.Entry<String, String> entry : correlationIds.entrySet()) {
                    MDC.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    public void beforeSend(ActorRef to, Object message, ActorRef from) {
        if (correlationsExists() && message instanceof Manuscript) {
            CorrelationHeader.setHeader((Manuscript) message, this.correlationIds);
        }
    }

    @Override
    public void afterReceive(ActorRef from, Object message, Actor self) {
        if (correlationsExists()) {
            for (Map.Entry<String, String> entry : correlationIds.entrySet()) {
                MDC.remove(entry.getKey());
            }
        }
    }

    private boolean correlationsExists() {
        return this.correlationIds != null && !this.correlationIds.isEmpty();
    }
}
