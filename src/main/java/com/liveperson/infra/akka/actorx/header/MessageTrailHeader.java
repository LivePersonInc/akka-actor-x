package com.liveperson.infra.akka.actorx.header;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Amit Tal
 * @since 10/2/2014
 */
public class MessageTrailHeader {

    private static Logger logger = LoggerFactory.getLogger(MessageTrailHeader.class);

    public static final String HEADER_NAME = "MESSAGE_TRAIL";
    public static final String LINE_SEPERATOR = System.getProperty("line.separator");

    // TODO HOW TO DEAL WITH IMMUTABLE/MUTABLE

    @SuppressWarnings("unchecked")
    public static List<Trail> getHeader(Manuscript manuscript) {
        Map<String, Object> headers = manuscript.getHeaders();
        if (headers != null) {
            return (List<Trail>) headers.get(HEADER_NAME);
        }
        else {
            logger.warn("Manuscript headers is null");
            return null;
        }
    }

    public static void setHeader(Manuscript manuscript, List<Trail>value) {
        Map<String, Object> headers = manuscript.getHeaders();
        if (headers != null) {
            headers.put(HEADER_NAME, value);
        }
        else {
            logger.warn("Manuscript headers is null");
        }
    }

    public static boolean exists(Manuscript manuscript) {
        Map<String, Object> headers = manuscript.getHeaders();
        if (headers != null) {
            return headers.containsKey(HEADER_NAME);
        }
        else {
            logger.warn("Manuscript headers is null");
            return false;
        }
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
