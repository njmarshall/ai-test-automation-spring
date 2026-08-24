package com.njmarshall.aitest.projects;

import com.njmarshall.aitest.projects.healthcare.client.PriorAuthApi;
import com.njmarshall.aitest.projects.healthcare.factory.PriorAuthFactory;
import com.njmarshall.aitest.projects.healthcare.model.PriorAuthStatus;
import com.njmarshall.aitest.shared.client.ApiResponse;
import com.njmarshall.aitest.shared.client.AsyncApiClient;
import com.njmarshall.aitest.shared.client.SyncApiClient;
import com.njmarshall.aitest.shared.client.poll.FixedDelayStrategy;
import com.njmarshall.aitest.shared.client.poll.PollStrategy;
import com.njmarshall.aitest.shared.client.poll.Poller;
import com.njmarshall.aitest.shared.client.poll.TerminalStateResolver;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end at the facade level: submit a prior auth, then poll until the payer's decision is
 * terminal. Uses hand-written fakes for the two segregated interfaces, driven by a real Poller
 * over a scripted status sequence, so the terminal-state wiring is genuinely exercised without a
 * live server.
 */
class PriorAuthWorkflowTest {

    /** Fake sync client that echoes a fixed submit response. */
    private static SyncApiClient submitReturning(ApiResponse response) {
        return new SyncApiClient() {
            @Override public ApiResponse get(String path) { return response; }
            @Override public ApiResponse post(String path, Object body) { return response; }
            @Override public ApiResponse put(String path, Object body) { return response; }
            @Override public ApiResponse delete(String path) { return response; }
            @Override public String serverName() { return "healthcare"; }
        };
    }

    /** Fake async client that walks a scripted status sequence through a real Poller. */
    private static AsyncApiClient scriptedStatuses(String... statuses) {
        Deque<ApiResponse> queue = new ArrayDeque<>();
        for (String s : statuses) {
            queue.add(new ApiResponse(200, "{\"status\":\"" + s + "\"}"));
        }
        Poller poller = new Poller(millis -> { });

        return new AsyncApiClient() {
            @Override
            public ApiResponse pollUntil(String path, Predicate<ApiResponse> condition, PollStrategy strategy) {
                return poller.pollUntil(queue::poll, condition, strategy);
            }

            @Override
            public ApiResponse pollUntilTerminal(String path, Function<ApiResponse, String> stateOf,
                                                 TerminalStateResolver terminal, PollStrategy strategy) {
                return pollUntil(path, r -> terminal.isTerminal(stateOf.apply(r)), strategy);
            }

            @Override
            public ApiResponse pollWithBackoff(String path, Predicate<ApiResponse> condition) {
                return pollUntil(path, condition, new FixedDelayStrategy(0, statuses.length));
            }

            @Override
            public String serverName() { return "healthcare"; }
        };
    }

    @Test
    void submitThenPollUntilTerminalDecision() {
        SyncApiClient sync = submitReturning(new ApiResponse(201, "{\"id\":\"pa-1\"}"));
        AsyncApiClient async = scriptedStatuses("RECEIVED", "IN_REVIEW", "APPROVED");
        PriorAuthApi api = new PriorAuthApi(sync, async);

        ApiResponse submit = api.submit(new PriorAuthFactory().build());
        String id = submit.field("id");
        assertThat(id).isEqualTo("pa-1");

        ApiResponse decision = api.awaitDecision(id, new FixedDelayStrategy(0, 5));

        assertThat(decision.field("status")).isEqualTo("APPROVED");
        assertThat(PriorAuthStatus.isTerminalState(decision.field("status"))).isTrue();
    }

    @Test
    void backoffVariantAlsoReachesTerminalDecision() {
        SyncApiClient sync = submitReturning(new ApiResponse(201, "{\"id\":\"pa-2\"}"));
        AsyncApiClient async = scriptedStatuses("PENDING", "DENIED");
        PriorAuthApi api = new PriorAuthApi(sync, async);

        ApiResponse decision = api.awaitDecision("pa-2");

        assertThat(decision.field("status")).isEqualTo("DENIED");
    }
}
