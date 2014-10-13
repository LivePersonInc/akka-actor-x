package com.liveperson.interaction.infra.akka.actorx.extension;

import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.ExtensionIdProvider;

/**
 * @author Amit Tal
 * @since 9/29/2014
 */
public class ActorXExtensionProvider extends AbstractExtensionId<ActorXExtension> implements ExtensionIdProvider {

    public static final ActorXExtensionProvider actorXExtensionProvider = new ActorXExtensionProvider();

    private ActorXExtensionProvider() {}

    public ActorXExtensionProvider lookup() {
        return ActorXExtensionProvider.actorXExtensionProvider;
    }

    public ActorXExtension createExtension(ExtendedActorSystem system) {
        return new ActorXExtension(system);
    }
}
