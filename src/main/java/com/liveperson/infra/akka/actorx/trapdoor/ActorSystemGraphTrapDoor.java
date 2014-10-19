package com.liveperson.infra.akka.actorx.trapdoor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Amit Tal
 * @since 10/13/2014
 */
public class ActorSystemGraphTrapDoor extends AbstractActor {

    private Logger logger = LoggerFactory.getLogger(ActorSystemGraphTrapDoor.class);

    public static Props props() {
        return Props.create(ActorSystemGraphTrapDoor.class);
    }

    public static class EDGE {

        private final ActorRef from;
        private final Class message;
        private final ActorRef to;

        public EDGE(ActorRef from, Class message, ActorRef to) {
            this.from = from;
            this.message = message;
            this.to = to;
        }

        public ActorRef getFrom() {
            return from;
        }

        public Class getMessage() {
            return message;
        }

        public ActorRef getTo() {
            return to;
        }


        @Override
        public String toString() {
            // TODO Enhance to string to use actor-ref 'path'
            final StringBuilder sb = new StringBuilder("EDGE{");
            sb.append("from=").append(from);
            sb.append(", message=").append(message);
            sb.append(", to=").append(to);
            sb.append('}');
            return sb.toString();
        }
    }

    // TODO Think of better structure
    private Set<EDGE> graph;

    public ActorSystemGraphTrapDoor() {
        this.graph = new HashSet<>();
        receive(ReceiveBuilder.match(EDGE.class, this::receivedEdge)
                .build());
    }

    private void receivedEdge(EDGE receivedEdge) {
        graph.add(receivedEdge);
    }


    private void dumpEdges() {
        if (graph != null) {
            graph.stream().forEach(edge -> logger.info("{}", edge));
        }
        else {
            logger.info("No edges");
        }
    }

}
