package com.liveperson.infra.akka.actorx.role;

import akka.actor.Actor;
import akka.actor.ActorRef;
import org.slf4j.MDC;

/**
 * @author Amit Tal
 * @since 9/18/2014
 */
public class AkkaSourceMdcRole implements Role {

    private final String MDC_AKKA_SOURCE = "akkaSource";

    @Override
    public void beforeReceive(ActorRef from, Object message, Actor self) {
        MDC.put(MDC_AKKA_SOURCE, self.self().path().toStringWithoutAddress());
    }

    @Override
    public void afterReceive(ActorRef from, Object message, Actor self) {
        MDC.remove(MDC_AKKA_SOURCE);
    }
}
