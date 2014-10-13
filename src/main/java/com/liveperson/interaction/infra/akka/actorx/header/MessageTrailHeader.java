package com.liveperson.interaction.infra.akka.actorx.header;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amit Tal
 * @since 10/2/2014
 */
public class MessageTrailHeader {

    public static final String HEADER_NAME = "MESSAGE_TRAIL";
    public static final String LINE_SEPERATOR = System.getProperty("line.separator");


    @SuppressWarnings("unchecked")
    public static List<Trail> getHeader(Manuscript manuscript) {
        return (List<Trail>) manuscript.get(HEADER_NAME);
    }

    public static void setHeader(Manuscript manuscript, List<Trail>value) {
        manuscript.put(HEADER_NAME, value);
    }

    public static boolean exists(Manuscript manuscript) {
        return manuscript.containsKey(HEADER_NAME);
    }


    // TODO Improve performance
    public static String getMessageTrailString(List<Trail> messageTrail) {
        StringBuilder buffer = new StringBuilder();
        if (messageTrail != null && !messageTrail.isEmpty()) {
            buffer.append(LINE_SEPERATOR).append(Trail.TRAIL_HEADER);
            for (Trail trail : messageTrail) {
                buffer.append(LINE_SEPERATOR).append(trail.getTrailString());
            }
            buffer.append(LINE_SEPERATOR).append(Trail.TRAIL_FOOTER);
        }
        return buffer.toString();
    }

    public static class Trail implements Serializable {

        final String actorName;
        final String messageClassName;
        final String fromActorName;

        public Trail(String actorName, Class messageClass, String fromActorName) {
            this.actorName = actorName;
            this.messageClassName = getClassName(messageClass);
            this.fromActorName = fromActorName;
        }

        String getClassName(Class messageClass) {
            String className = messageClass.getName();
            int lastDot = className.lastIndexOf(".");
            if (lastDot > 0) {
                className = className.substring(lastDot + 1);
            }
            return className;
        }

        static final String TRAIL_HEADER =
                "---------------------------------------------------------------------------------------------------------" + LINE_SEPERATOR +
                "| actor-name                     | from-actor-name                | message-class-name                  |" + LINE_SEPERATOR +
                "|-------------------------------------------------------------------------------------------------------|";

        static final String TRAIL_FOOTER =
                "|-------------------------------------------------------------------------------------------------------|";

        String getTrailString() {
            return String.format("| %-30s | %-30s | %-35s |", actorName, fromActorName, messageClassName);
        }

    }
}
