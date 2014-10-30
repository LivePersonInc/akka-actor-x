package com.liveperson.infra.akka.actorx.role;

import akka.actor.Actor;
import akka.actor.ActorRef;

import java.util.List;
import java.util.ListIterator;

/**
 * @author Amit Tal
 * @since 9/21/2014
 */
public class Roles implements Role {

    final private List<Role> roles;

    public Roles(List<Role> roles) {
        this.roles = roles;
    }

    @Override
    public void beforeReceive(ActorRef from, Object message, Actor self) {
        if (this.roles != null) {
            this.roles.stream().forEach((role) -> role.beforeReceive(from, message, self));
        }
    }

    @Override
    public void afterReceive(ActorRef from, Object message, Actor self) {
        if (this.roles != null) {
            ListIterator<Role> listIterator = this.roles.listIterator(this.roles.size());
            while (listIterator.hasPrevious()) {
                Role previous = listIterator.previous();
                previous.afterReceive(from, message, self);
            }
        }
    }

    @Override
    public void beforeSend(ActorRef to, Object message, ActorRef from) {
        if (this.roles != null) {
            this.roles.stream().forEach((role) -> role.beforeSend(to, message, from));
        }
    }

    @Override
    public void afterSend(ActorRef to, Object message, ActorRef from) {
        if (this.roles != null) {
            ListIterator<Role> listIterator = this.roles.listIterator(this.roles.size());
            while (listIterator.hasPrevious()) {
                Role previous = listIterator.previous();
                previous.afterSend(to, message, from);
            }
        }
    }

    @Override
    public void afterActorOf(ActorRef actorRef) {
        if (this.roles != null) {
            this.roles.stream().forEach((role) -> role.afterActorOf(actorRef));
        }
    }

    public List<Role> getRoles() {
        return roles;
    }
}
