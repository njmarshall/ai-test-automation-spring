package com.njmarshall.aitest.shared.testgen;

/**
 * One AI-generated test case, deserialized from the model's JSON output.
 *
 * @param name            short test name
 * @param method          HTTP method under test
 * @param path            path under test
 * @param category        e.g. "happy-path", "validation", "authz", "boundary"
 * @param description     what the test asserts, in one sentence
 * @param expectedStatus  expected HTTP status code
 */
public record GeneratedTestCase(
        String name,
        String method,
        String path,
        String category,
        String description,
        int expectedStatus) {
}
