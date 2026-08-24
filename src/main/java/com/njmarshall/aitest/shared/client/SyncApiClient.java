package com.njmarshall.aitest.shared.client;

/**
 * INTERFACE SEGREGATION (the "I" in SOLID), part 1 of 2.
 *
 * <p>The synchronous verbs live on their own interface. A test that only sends a POST and checks
 * the response depends on this, and nothing forces it to see the polling methods it will never
 * call. Compare a single fat ApiClient interface, which would drag async concerns into every
 * simple test and every mock.
 */
public interface SyncApiClient {

    ApiResponse get(String path);

    ApiResponse post(String path, Object body);

    ApiResponse put(String path, Object body);

    ApiResponse delete(String path);

    /** The logical server name this client targets, e.g. "healthcare". */
    String serverName();
}
