package com.liveperson.infra.akka.actorx.extension;

import akka.actor.ActorRef;
import akka.actor.ExtendedActorSystem;
import akka.actor.Extension;
import com.liveperson.infra.akka.actorx.staff.CastTraceAssistant;
import com.typesafe.config.Config;

import java.util.HashSet;
import java.util.List;

/**
 * @author Amit Tal
 * @since 9/29/2014
 */
public class ActorXExtension implements Extension {


    private ExtendedActorSystem system;

    // Staff Actors
    private ActorRef castTraceActor;

    ActorXExtension(ExtendedActorSystem system) {
        this.system = system;
        configureExtension();
    }

    private void configureExtension() {
        ActorXConfig.configure(system.settings().config());
        configureStaff();
    }

    private void configureStaff() {
        if (ActorXConfig.isCastTraceActive()) {
            this.castTraceActor = system.actorOf(CastTraceAssistant.props(), "cast-trace");
        }
    }

    public ActorRef getCastTraceActor() {
        return this.castTraceActor;
    }
}
