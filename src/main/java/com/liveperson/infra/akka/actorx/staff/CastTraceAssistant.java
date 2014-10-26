package com.liveperson.infra.akka.actorx.staff;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
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

    private Map<ActorRef, String> lookupActorClassNameByRef;
    private Set<Scene> unprocessedScenes;
    private Map<String, Map<String, Set<String>>> castConnectionList;
    private int numOfReceivedScenes;

    public static Props props() {
        return Props.create(CastTraceAssistant.class);
    }


    public CastTraceAssistant() {
        this.lookupActorClassNameByRef = new HashMap<>();
        this.castConnectionList = new HashMap<>();
        this.unprocessedScenes = new HashSet<>();
        this.numOfReceivedScenes = 0;
        receive(ReceiveBuilder
                .match(Scene.class, this::receiveScene)
                .match(LogCast.class, this::logCast)
                .build());
    }

    private void receiveScene(Scene scene) {
        this.numOfReceivedScenes++;
        lookupActorClassNameByRef.put(scene.getFromActorSelfRef(), scene.getFromActorClassName());
        final Map<String, Set<String>> previousValue = castConnectionList.putIfAbsent(scene.getFromActorClassName(), new HashMap<>());

        if (!(addActorConnection(scene))) {
            unprocessedScenes.add(scene);
        }
    }

    private void processUnprocessedScenes() {
        Iterator<Scene> sceneIterator = unprocessedScenes.iterator();
        while (sceneIterator.hasNext()) {
            Scene scene = sceneIterator.next();
            if (addActorConnection(scene)) {
                sceneIterator.remove();
            }
        }
    }

    private boolean addActorConnection(Scene scene) {

        // If to actor ref is in actor lookup list then can process scene
        if (lookupActorClassNameByRef.containsKey(scene.getToActorRef())) {

            // to actor class name
            String toActorClassName = lookupActorClassNameByRef.get(scene.getToActorRef());

            // If needed, add to actor as connection
            Map<String, Set<String>> actorConnections = castConnectionList.get(scene.getFromActorClassName());
            actorConnections.putIfAbsent(toActorClassName, new HashSet<>());

            // Add scene message
            Set<String> messages = actorConnections.get(toActorClassName);
            messages.add(scene.getMessageClassName());
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
        if (!unprocessedScenes.isEmpty()) {

            buffer.append("The following contains unknown actor classes (actor-refs)")
                  .append(ActorXConfig.LINE_SEPARATOR)
                  .append("---------------------------------------------------------")
                  .append(ActorXConfig.LINE_SEPARATOR).append(ActorXConfig.LINE_SEPARATOR);

            unprocessedScenes.stream().forEach(scene -> {
                buffer.append(getClassName(scene.fromActorClassName)).append(ActorXConfig.LINE_SEPARATOR);
                buffer.append("|").append(ActorXConfig.LINE_SEPARATOR);
                String actorRefPath = (scene.getToActorRef()) != null ? scene.getToActorRef().path().name() : null;
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
            for (Scene scene : unprocessedScenes) {

                String fromActor = scene.fromActorClassName;
                String toActor = (scene.getToActorRef()) != null ? scene.getToActorRef().path().name() : "unknown:" + (index++);
                String message = scene.getMessageClassName();
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

    public static class Scene implements StaffMessage {

        private final String fromActorClassName;
        private final ActorRef fromActorSelfRef;
        private final String messageClassName;
        private final ActorRef toActorRef;

        public Scene(String fromActorClassName, ActorRef fromActorSelfRef) {
            this.fromActorClassName = fromActorClassName;
            this.fromActorSelfRef = fromActorSelfRef;
            this.messageClassName = null;
            this.toActorRef = null;
        }

        public Scene(String fromActorClassName, ActorRef fromActorSelfRef, ActorRef toActorRef) {
            this.fromActorClassName = fromActorClassName;
            this.fromActorSelfRef = fromActorSelfRef;
            this.messageClassName = null;
            this.toActorRef = toActorRef;
        }

        public Scene(String fromActorClassName, ActorRef fromActorSelfRef, String messageClassName, ActorRef toActorRef) {
            this.fromActorClassName = fromActorClassName;
            this.fromActorSelfRef = fromActorSelfRef;
            this.messageClassName = messageClassName;
            this.toActorRef = toActorRef;
        }

        public String getFromActorClassName() {
            return fromActorClassName;
        }

        public ActorRef getFromActorSelfRef() {
            return fromActorSelfRef;
        }

        public String getMessageClassName() {
            if (messageClassName == null) {
                return "[actorOf]";
            }
            return messageClassName;
        }

        public ActorRef getToActorRef() {
            return toActorRef;
        }
    }
}
