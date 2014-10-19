package com.liveperson.infra.akka.actorx.header;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Amit Tal
 * @since 10/2/2014
 */
public class SimpleManuscript implements Manuscript {

    private Map<String, Object> headers;

    public SimpleManuscript() {
        headers = new HashMap<>();
    }

    @Override
    public Map<String, Object> getHeaders() {
        return this.headers;
    }
}
