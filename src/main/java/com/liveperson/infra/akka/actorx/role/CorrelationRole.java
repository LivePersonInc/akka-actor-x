package com.liveperson.infra.akka.actorx.role;

import akka.actor.Actor;
import akka.actor.ActorRef;
import com.liveperson.infra.akka.actorx.extension.ActorXConfig;
import com.liveperson.infra.akka.actorx.header.CorrelationHeader;
import com.liveperson.infra.akka.actorx.header.Manuscript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Amit Tal
 * @since 9/21/2014
 */
public class CorrelationRole implements Role {

    private Logger logger = LoggerFactory.getLogger(CorrelationRole.class);

    private HashMap<String, String> correlationIds;

    // TODO Should correlation MDC be here?
    // TODO Can use optional? (see correlationsExists)

    // TODO HOW TO DEAL WITH IMMUTABLE/MUTABLE (copy correlationIds?)

    @Override
    public void beforeReceive(ActorRef from, Object message, Actor self) {
        this.correlationIds = null;
        if (message instanceof Manuscript) {

            HashMap<String, String> correlationHeader = CorrelationHeader.getHeader((Manuscript) message);
            if (correlationHeader != null) {
                this.correlationIds = new HashMap<>(correlationHeader);
            }

            // Add correlation ids
            if (correlationsExists()) {
                for (Map.Entry<String, String> entry : correlationIds.entrySet()) {
                    MDC.put(entry.getKey(), entry.getValue());
                }
            }
        }

        // New request-id feature
        if (ActorXConfig.isRoleCorrelationCreateNewRequest()) {

            if (this.correlationIds == null) {
                this.correlationIds = new HashMap<>(1);
            }

            if (!(correlationIds.containsKey(ActorXConfig.getRoleCorrelationCreateNewRequestHeaderName()))) {
                String requestId = UUID.randomUUID().toString();
                correlationIds.put(ActorXConfig.getRoleCorrelationCreateNewRequestHeaderName(), requestId);
                logger.trace("Created new request-id header [{}] with id [{}]", ActorXConfig.getRoleCorrelationCreateNewRequestHeaderName(), requestId);
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
