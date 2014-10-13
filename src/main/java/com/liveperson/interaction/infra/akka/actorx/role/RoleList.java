package com.liveperson.interaction.infra.akka.actorx.role;

import akka.actor.Actor;
import akka.actor.ActorRef;

import java.util.List;
import java.util.ListIterator;

/**
 * @author Amit Tal
 * @since 9/21/2014
 */
public class RoleList implements Role {

    final private List<Role> roles;

    public RoleList(List<Role> roles) {
        // TODO VALIDATION
        this.roles = roles;
    }

    @Override
    public void beforeReceive(ActorRef from, Object message, Actor self) {
        this.roles.stream().forEach((mask) -> mask.beforeReceive(from, message, self));
    }

    @Override
    public void afterReceive(ActorRef from, Object message, Actor self) {
        ListIterator<Role> listIterator = this.roles.listIterator(this.roles.size());
        while (listIterator.hasPrevious()) {
            Role previous = listIterator.previous();
            previous.afterReceive(from, message, self);
        }
    }

    @Override
    public void beforeSend(ActorRef to, Object message, ActorRef from) {
        this.roles.stream().forEach((mask) -> mask.beforeSend(to, message, from));
    }

    @Override
    public void afterSend(ActorRef to, Object message, ActorRef from) {
        ListIterator<Role> listIterator = this.roles.listIterator(this.roles.size());
        while (listIterator.hasPrevious()) {
            Role previous = listIterator.previous();
            previous.afterSend(to, message, from);
        }
    }

    @Override
    public void afterActorOf(ActorRef actorRef) {
        this.roles.stream().forEach((mask) -> mask.afterActorOf(actorRef));
    }

    public List<Role> getRoles() {
        return roles;
    }
}
