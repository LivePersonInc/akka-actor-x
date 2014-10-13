package com.liveperson.infra.akka.actorx.extension;

import akka.actor.ExtendedActorSystem;
import akka.actor.Extension;

/**
 * @author Amit Tal
 * @since 9/29/2014
 */
public class ActorXExtension implements Extension {

    private ExtendedActorSystem system;

    ActorXExtension(ExtendedActorSystem system) {
        this.system = system;
    }
}
