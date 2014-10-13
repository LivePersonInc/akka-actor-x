package com.liveperson.infra.akka.actorx.header;

import java.util.HashMap;

/**
 * @author Amit Tal
 * @since 10/2/2014
 */
public class CorrelationHeader {

    // TODO CAN USE OPTIONAL???

    private static final String HEADER_NAME = "CORRELATION_HEADER";

    @SuppressWarnings("unchecked")
    public static HashMap<String, String> getHeader(Manuscript manuscript) {
        return (HashMap<String, String>) manuscript.get(HEADER_NAME);
    }

    public static void setHeader(Manuscript manuscript, HashMap<String, String> value) {
        manuscript.put(HEADER_NAME, value);
    }

    public static void setCorrelation(Manuscript manuscript, String key, String value) {
        HashMap<String, String> header = getHeader(manuscript);
        if (header == null) {
            header = new HashMap<>();
            setHeader(manuscript, header);
        }
        header.put(key, value);
    }

    public static String getCorrelation(Manuscript manuscript, String key) {
        HashMap<String, String> header = getHeader(manuscript);
        String value = null;
        if (header != null) {
            value = header.get(key);
        }
        return value;
    }

    public static boolean exists(Manuscript manuscript) {
        return manuscript.containsKey(HEADER_NAME);
    }
}
