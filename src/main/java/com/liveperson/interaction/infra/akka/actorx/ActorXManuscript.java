package com.liveperson.interaction.infra.akka.actorx;

import com.liveperson.interaction.infra.akka.actorx.header.SimpleManuscript;

/**
 * @author Amit Tal
 * @since 9/21/2014
 */
public class ActorXManuscript extends SimpleManuscript {

    final private Object message;

    public ActorXManuscript(Object message) {
        super();
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActorXManuscript{");
        String bodyClass = message == null ? "NULL" : message.getClass().toString();
        sb.append("message=").append(bodyClass);
        sb.append('}');
        return sb.toString();
    }
}
