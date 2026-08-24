package com.njmarshall.aitest.shared;

import com.njmarshall.aitest.projects.healthcare.model.PriorAuthStatus;
import com.njmarshall.aitest.shared.client.ApiResponse;
import com.njmarshall.aitest.shared.client.poll.FixedDelayStrategy;
import com.njmarshall.aitest.shared.client.poll.PollException;
import com.njmarshall.aitest.shared.client.poll.Poller;
import com.njmarshall.aitest.shared.client.poll.Sleeper;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the poll loop in isolation. Because the fetch is a Supplier and the Sleeper is a no-op,
 * these run instantly and never touch the network. This is the payoff of separating the loop from
 * the HTTP transport.
 */
class PollerTest {

    private static final Sleeper NO_SLEEP = millis -> { };

    /** A fetch that returns each scripted response in turn, then repeats the last one. */
    private static Supplier<ApiResponse> scriptOf(ApiResponse... responses) {
        Deque<ApiResponse> queue = new ArrayDeque<>(List.of(responses));
        final ApiResponse[] last = { responses[responses.length - 1] };
        return () -> {
            ApiResponse next = queue.poll();
            if (next != null) {
                last[0] = next;
            }
            return last[0];
        };
    }

    private static ApiResponse withStatus(String status) {
        return new ApiResponse(200, "{\"status\":\"" + status + "\"}");
    }

    @Test
    void stopsAtFirstResponseThatMeetsTheCondition() {
        Poller poller = new Poller(NO_SLEEP);
        Supplier<ApiResponse> fetch = scriptOf(
                withStatus("RECEIVED"),
                withStatus("IN_REVIEW"),
                withStatus("APPROVED"));

        ApiResponse result = poller.pollUntil(
                fetch,
                response -> PriorAuthStatus.isTerminalState(response.field("status")),
                new FixedDelayStrategy(0, 5));

        assertThat(result.field("status")).isEqualTo("APPROVED");
    }

    @Test
    void throwsPollExceptionWhenAttemptsExhaust() {
        Poller poller = new Poller(NO_SLEEP);
        Supplier<ApiResponse> neverTerminal = scriptOf(withStatus("IN_REVIEW"));

        assertThatThrownBy(() -> poller.pollUntil(
                neverTerminal,
                response -> PriorAuthStatus.isTerminalState(response.field("status")),
                new FixedDelayStrategy(0, 3)))
                .isInstanceOf(PollException.class)
                .hasMessageContaining("3 attempts");
    }
}
