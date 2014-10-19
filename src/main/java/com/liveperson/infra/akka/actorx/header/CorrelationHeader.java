package com.liveperson.infra.akka.actorx.header;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Amit Tal
 * @since 10/2/2014
 */
public class CorrelationHeader {

    private static Logger logger = LoggerFactory.getLogger(CorrelationHeader.class);

    // TODO CAN USE OPTIONAL???
    // TODO HOW TO DEAL WITH IMMUTABLE/MUTABLE

    private static final String HEADER_NAME = "CORRELATION_HEADER";

    @SuppressWarnings("unchecked")
    public static HashMap<String, String> getHeader(Manuscript manuscript) {
        Map<String, Object> headers = manuscript.getHeaders();
        if (headers != null) {
            return (HashMap<String, String>) headers.get(HEADER_NAME);
        }
        else {
            logger.warn("Manuscript headers is null");
            return null;
        }
    }

    public static void setHeader(Manuscript manuscript, HashMap<String, String> value) {
        Map<String, Object> headers = manuscript.getHeaders();
        if (headers != null) {
            headers.put(HEADER_NAME, value);
        }
        else {
            logger.warn("Manuscript headers is null");
        }
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
        Map<String, Object> headers = manuscript.getHeaders();
        if (headers != null) {
            return headers.containsKey(HEADER_NAME);
        }
        else {
            logger.warn("Manuscript headers is null");
            return false;
        }
    }
}
