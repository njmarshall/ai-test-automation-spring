package com.njmarshall.aitest.shared.testgen;

import java.util.List;

/**
 * A single flattened operation pulled out of an OpenAPI spec. This is the neutral, provider-free
 * shape we hand to the LLM, so the prompt does not depend on swagger-parser's object model.
 *
 * @param method           HTTP method, upper case, e.g. "POST"
 * @param path             templated path, e.g. "/prior-authorizations/{id}"
 * @param operationId      stable id from the spec, may be null
 * @param summary          short human description, may be null
 * @param requiredParams   names of required parameters (path/query/header)
 * @param hasRequestBody   whether the operation declares a request body
 */
public record ApiOperation(
        String method,
        String path,
        String operationId,
        String summary,
        List<String> requiredParams,
        boolean hasRequestBody) {
}
