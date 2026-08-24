package com.njmarshall.aitest.shared.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A thin, immutable view of an HTTP response: the status code and the raw body. Kept deliberately
 * small so poll predicates and validators can inspect it without depending on Spring types.
 *
 * @param statusCode HTTP status code
 * @param body       raw response body, possibly empty
 */
public record ApiResponse(int statusCode, String body) {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public boolean is2xx() {
        return statusCode >= 200 && statusCode < 300;
    }

    /** Parse the body as JSON. Throws if the body is not valid JSON. */
    public JsonNode json() {
        try {
            return MAPPER.readTree(body == null ? "" : body);
        } catch (Exception e) {
            throw new IllegalStateException("Response body is not valid JSON: " + body, e);
        }
    }

    /** Convenience reader for a top-level string field, e.g. status().  Returns "" if absent. */
    public String field(String name) {
        return json().path(name).asText("");
    }
}
