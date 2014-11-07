package com.liveperson.infra.akka.actorx.staff;

import akka.actor.AbstractActor;
import akka.actor.ActorPath;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.liveperson.infra.akka.actorx.extension.ActorXConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Amit Tal
 * @since 10/20/2014
 */
public class CastTraceAssistant extends AbstractActor {

    private Logger logger = LoggerFactory.getLogger(CastTraceAssistant.class);

    private Map<ActorPath, String> lookupActorClassNameByPath;
    private Set<Edge> unprocessedEdges;
    private Map<String, Map<String, Set<String>>> castConnectionList;
    private int numOfReceivedScenes;

    public static Props props() {
        return Props.create(CastTraceAssistant.class);
    }


    public CastTraceAssistant() {
        this.lookupActorClassNameByPath = new HashMap<>();
        this.castConnectionList = new HashMap<>();
        this.unprocessedEdges = new HashSet<>();
        this.numOfReceivedScenes = 0;
        receive(ReceiveBuilder
                .match(Edge.class, this::receiveEdge)
                .match(LogCast.class, this::logCast)
                .build());
    }

    private void receiveEdge(Edge edge) {
        this.numOfReceivedScenes++;
        lookupActorClassNameByPath.put(edge.getFromActorPath(), edge.getFromActorClassName());
        final Map<String, Set<String>> previousValue = castConnectionList.putIfAbsent(edge.getFromActorClassName(), new HashMap<>());

        if (!(addActorConnection(edge))) {
            unprocessedEdges.add(edge);
        }
    }

    private void processUnprocessedScenes() {
        Iterator<Edge> sceneIterator = unprocessedEdges.iterator();
        while (sceneIterator.hasNext()) {
            Edge edge = sceneIterator.next();
            if (addActorConnection(edge)) {
                sceneIterator.remove();
            }
        }
    }

    private boolean addActorConnection(Edge edge) {

        // If to actor ref is in actor lookup list then can process scene
        if (lookupActorClassNameByPath.containsKey(edge.getToActorPath())) {

            // to actor class name
            String toActorClassName = lookupActorClassNameByPath.get(edge.getToActorPath());

            // If needed, add to actor as connection
            Map<String, Set<String>> actorConnections = castConnectionList.get(edge.getFromActorClassName());
            actorConnections.putIfAbsent(toActorClassName, new HashSet<>());

            // Add scene message
            Set<String> messages = actorConnections.get(toActorClassName);
            messages.add(edge.getMessageClassName());
            return true;
        }
        else {
            return false;
        }
    }

    private void logCast(LogCast logCast) {
        //
        processUnprocessedScenes();

        String stringForActorXUI = getStringForActorXUI();
        logger.info(stringForActorXUI);

        String message = castTraceToString();
        logger.info(message);

        // TODO Send or not?
        /*if (sender() != null && sender() != ActorRef.noSender()) {
            sender().tell(new LogCastResult(message), self());
        }*/
    }

    // TODO Change to better logic so graph print is more logical (like BFS for example)
    private String castTraceToString() {

        StringBuffer buffer = new StringBuffer(ActorXConfig.LINE_SEPARATOR);

        // Header
        buffer.append("Cast connection network is as follows ").
               append("(processed #").append(numOfReceivedScenes).
               append(" events)").append(ActorXConfig.LINE_SEPARATOR).
               append("-------------------------------------").
               append(ActorXConfig.LINE_SEPARATOR).append(ActorXConfig.LINE_SEPARATOR);
        buffer.append("<FROM-ACTOR>").append(ActorXConfig.LINE_SEPARATOR);
        buffer.append("|---  ").append("<TO-ACTOR>").append(ActorXConfig.LINE_SEPARATOR);
        buffer.append("|------   ").append("<MESSAGES>").append(ActorXConfig.LINE_SEPARATOR);
        buffer.append(ActorXConfig.LINE_SEPARATOR);
        buffer.append(ActorXConfig.LINE_SEPARATOR);

        for (Map.Entry<String, Map<String, Set<String>>> actorConnectionEntry : castConnectionList.entrySet()) {

            // From Actor
            buffer.append(getClassName(actorConnectionEntry.getKey())).append(ActorXConfig.LINE_SEPARATOR);

            // Connections
            Map<String, Set<String>> connections = actorConnectionEntry.getValue();
            for (Map.Entry<String, Set<String>> connectionEntry : connections.entrySet()) {

                // To Actor
                buffer.append("|").append(ActorXConfig.LINE_SEPARATOR);
                buffer.append("|---  ").append(getClassName(connectionEntry.getKey())).append(ActorXConfig.LINE_SEPARATOR);

                // Messages
                Set<String> messages = connectionEntry.getValue();
                for (String message : messages) {

                    // Message
                    buffer.append("|------   ").append(getClassName(message)).append(ActorXConfig.LINE_SEPARATOR);
                }
            }

            buffer.append(ActorXConfig.LINE_SEPARATOR);
            buffer.append(ActorXConfig.LINE_SEPARATOR);
        }

        // Unprocessed scene
        if (!unprocessedEdges.isEmpty()) {

            buffer.append("The following contains unknown actor classes (actor-refs)")
                  .append(ActorXConfig.LINE_SEPARATOR)
                  .append("---------------------------------------------------------")
                  .append(ActorXConfig.LINE_SEPARATOR).append(ActorXConfig.LINE_SEPARATOR);

            unprocessedEdges.stream().forEach(scene -> {
                buffer.append(getClassName(scene.fromActorClassName)).append(ActorXConfig.LINE_SEPARATOR);
                buffer.append("|").append(ActorXConfig.LINE_SEPARATOR);
                String actorRefPath = (scene.getToActorPath()) != null ? scene.getToActorPath().name() : null;
                buffer.append("|---  ").append(actorRefPath).append(ActorXConfig.LINE_SEPARATOR);
                buffer.append("|------   ").append(getClassName(scene.getMessageClassName())).append(ActorXConfig.LINE_SEPARATOR);
                buffer.append(ActorXConfig.LINE_SEPARATOR);
            });
        }

        return buffer.toString();
    }

    private String getClassName(String className) {
        String prettyName = className;
        if (className != null) {
            int lastDot = prettyName.lastIndexOf(".");
            if (lastDot > 0) {
                prettyName = prettyName.substring(lastDot + 1);
            }
        }
        return prettyName;
    }

    private String getStringForActorXUI() {

        StringBuffer result = new StringBuffer();
        result.append(ActorXConfig.LINE_SEPARATOR).append("****** actor-x-ui dump start ******").append(ActorXConfig.LINE_SEPARATOR);
        if (castConnectionList != null && !castConnectionList.isEmpty()) {

            StringBuffer verticesBuffer = new StringBuffer("Vertices:");
            StringBuffer edgesBuffer = new StringBuffer("Edges:");

            for (Map.Entry<String, Map<String, Set<String>>> castConnection : castConnectionList.entrySet()) {

                String fromActor = castConnection.getKey();
                verticesBuffer.append(fromActor).append(",");

                Map<String, Set<String>> connections = castConnection.getValue();
                for (Map.Entry<String, Set<String>> connection : connections.entrySet()) {

                    String toActor = connection.getKey();
                    for (String message : connection.getValue()) {
                        edgesBuffer.append(message).append("|").append(fromActor).append("|").append(toActor).append(",");
                    }
                }
            }

            int index = 0;
            for (Edge edge : unprocessedEdges) {

                String fromActor = edge.fromActorClassName;
                String toActor = (edge.getToActorPath()) != null ? edge.getToActorPath().name() : "unknown:" + (index++);
                String message = edge.getMessageClassName();
                edgesBuffer.append(message).append("|").append(fromActor).append("|").append(toActor).append(",");
            }

            verticesBuffer.deleteCharAt(verticesBuffer.length() - 1);
            edgesBuffer.deleteCharAt(edgesBuffer.length() - 1);

            result.append(verticesBuffer).append("###").append(edgesBuffer);
            result.append(ActorXConfig.LINE_SEPARATOR).append("****** actor-x-ui dump end ******").append(ActorXConfig.LINE_SEPARATOR);
        }
        return result.toString();
    }


    public static enum LogCast implements StaffMessage {
        INSTANCE
    }

    public static class LogCastResult implements StaffMessage {

        private final String log;

        public LogCastResult(String log) {
            this.log = log;
        }

        public String getLog() {
            return log;
        }
    }

    public static class Edge implements StaffMessage {

        private final String fromActorClassName;
        private final ActorPath fromActorPath;
        private final String messageClassName;
        private final ActorPath toActorPath;
        private final Date date;

        Edge(String fromActorClassName, ActorPath fromActorPath) {
            this(fromActorClassName, fromActorPath, null, null);
        }

        Edge(String fromActorClassName, ActorPath fromActorPath, ActorPath toActorPath) {
            this(fromActorClassName, fromActorPath, null, toActorPath);
        }

        public Edge(String fromActorClassName, ActorPath fromActorPath, String messageClassName, ActorPath toActorPath) {
            this.fromActorClassName = fromActorClassName;
            this.fromActorPath = fromActorPath;
            this.messageClassName = messageClassName;
            this.toActorPath = toActorPath;
            this.date = new Date();
        }

        public String getFromActorClassName() {
            return fromActorClassName;
        }

        public ActorPath getFromActorPath() {
            return fromActorPath;
        }

        public ActorPath getToActorPath() {
            return toActorPath;
        }

        public String getMessageClassName() {
            if (messageClassName == null) {
                return "[actorOf]";
            }
            return messageClassName;
        }
    }

    public static class ActorOfEdge extends Edge {
        public ActorOfEdge(String fromActorClassName, ActorPath fromActorPath, ActorPath toActorPath) {
            super(fromActorClassName, fromActorPath, null, toActorPath);
        }
    }

    public static class IdentifierEdge extends Edge {
        public IdentifierEdge(String fromActorClassName, ActorPath fromActorPath) {
            super(fromActorClassName, fromActorPath, null, null);
        }
    }
}
