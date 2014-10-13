package com.liveperson.infra.akka.actorx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Amit Tal
 * @since 9/29/2014
 */
public class ActorXDressingRoom {

    private static final Logger logger = LoggerFactory.getLogger(ActorXDressingRoom.class);
    private static final ThreadLocal<ActorXDirector> storage = new InheritableThreadLocal<>();

    public static void setActorXDirector(ActorXDirector actorXDirector) {
        storage.set(actorXDirector);
    }

    public static ActorXDirector getActorXDirector() {
        return storage.get();
    }

    public static void removeActorXDirector() {
        storage.remove();
    }
}
