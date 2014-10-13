package com.liveperson.interaction.infra.akka.actorx.role;

import akka.actor.Actor;
import akka.actor.ActorRef;

/**
 * @author Amit Tal
 * @since 9/18/2014
 */
public interface Role {

    default void beforeReceive(ActorRef from, Object message, Actor self) {};
    default void afterReceive(ActorRef from, Object message, Actor self) {};

    default void beforeSend(ActorRef to, Object message, ActorRef from) {};
    default void afterSend(ActorRef to, Object message, ActorRef from) {};

    default void afterActorOf(ActorRef actorRef) {};
}
