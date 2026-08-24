package com.njmarshall.aitest.shared.client;

import com.njmarshall.aitest.shared.client.poll.ExponentialBackoffStrategy;
import com.njmarshall.aitest.shared.client.poll.PollStrategy;
import com.njmarshall.aitest.shared.client.poll.Poller;
import com.njmarshall.aitest.shared.client.poll.Sleeper;
import com.njmarshall.aitest.shared.client.poll.TerminalStateResolver;
import com.njmarshall.aitest.shared.config.FrameworkProperties;
import org.springframework.web.client.RestClient;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * One class implements both {@link SyncApiClient} and {@link AsyncApiClient}. Interface
 * Segregation is about what callers depend on, not how many classes exist: a caller that only
 * needs synchronous verbs is handed a {@code SyncApiClient} reference and never sees the poll
 * methods, even though the same object underneath can do both.
 *
 * <p>Bound to exactly one server (one base URL). The factory makes one of these per configured
 * server, which is how a single test run can hit several back ends at once.
 */
public class RestApiClient implements SyncApiClient, AsyncApiClient {

    private final String serverName;
    private final RestClient restClient;
    private final FrameworkProperties.Poll pollDefaults;
    private final Poller poller;

    public RestApiClient(String serverName,
                         RestClient restClient,
                         FrameworkProperties.Poll pollDefaults,
                         Sleeper sleeper) {
        this.serverName = serverName;
        this.restClient = restClient;
        this.pollDefaults = pollDefaults;
        this.poller = new Poller(sleeper);
    }

    @Override
    public String serverName() {
        return serverName;
    }

    // ---- SyncApiClient -----------------------------------------------------

    @Override
    public ApiResponse get(String path) {
        return restClient.get().uri(path).exchange(this::toResponse);
    }

    @Override
    public ApiResponse post(String path, Object body) {
        return restClient.post().uri(path).body(body).exchange(this::toResponse);
    }

    @Override
    public ApiResponse put(String path, Object body) {
        return restClient.put().uri(path).body(body).exchange(this::toResponse);
    }

    @Override
    public ApiResponse delete(String path) {
        return restClient.delete().uri(path).exchange(this::toResponse);
    }

    /**
     * exchange() bypasses RestClient's default 4xx/5xx error handling, so we can observe error
     * responses as data instead of catching exceptions. That is what a test client wants. The
     * ExchangeFunction is a two-argument callback (request, response), hence both parameters.
     */
    private ApiResponse toResponse(
            org.springframework.http.HttpRequest request,
            RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response)
            throws java.io.IOException {
        int status = response.getStatusCode().value();
        String body = response.bodyTo(String.class);
        return new ApiResponse(status, body == null ? "" : body);
    }

    // ---- AsyncApiClient ----------------------------------------------------

    @Override
    public ApiResponse pollUntil(String path, Predicate<ApiResponse> condition, PollStrategy strategy) {
        return poller.pollUntil(() -> get(path), condition, strategy);
    }

    @Override
    public ApiResponse pollUntilTerminal(String path,
                                         Function<ApiResponse, String> stateOf,
                                         TerminalStateResolver terminal,
                                         PollStrategy strategy) {
        return pollUntil(path, response -> terminal.isTerminal(stateOf.apply(response)), strategy);
    }

    @Override
    public ApiResponse pollWithBackoff(String path, Predicate<ApiResponse> condition) {
        PollStrategy strategy = new ExponentialBackoffStrategy(
                pollDefaults.getInitialDelayMillis(),
                pollDefaults.getMaxDelayMillis(),
                pollDefaults.getMultiplier(),
                pollDefaults.getMaxAttempts());
        return pollUntil(path, condition, strategy);
    }
}
