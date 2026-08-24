package com.njmarshall.aitest.shared.client;

import com.njmarshall.aitest.shared.client.poll.PollStrategy;
import com.njmarshall.aitest.shared.client.poll.TerminalStateResolver;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * INTERFACE SEGREGATION, part 2 of 2.
 *
 * <p>Asynchronous, long-running workflows get their own interface. This models the real shape of
 * healthcare prior authorization and insurance claims adjudication: you submit a request, then the
 * resource sits in a non-terminal state (RECEIVED, PENDING, IN_REVIEW) until a human or a batch
 * job moves it to a terminal state (APPROVED, DENIED). The client's job is to poll a status
 * endpoint until it settles, without hammering the server.
 *
 * <p>Three strategies, three methods:
 * <ul>
 *   <li>{@link #pollUntil} polls a status endpoint until an arbitrary condition holds.</li>
 *   <li>{@link #pollUntilTerminal} polls until a state field reaches a terminal value. This is the
 *       one you reach for in adjudication tests.</li>
 *   <li>{@link #pollWithBackoff} is the convenience entry point that applies exponential backoff
 *       from configured defaults, so slow back ends are not overwhelmed.</li>
 * </ul>
 */
public interface AsyncApiClient {

    /** Poll {@code path} until {@code condition} is true, or fail once the strategy is exhausted. */
    ApiResponse pollUntil(String path, Predicate<ApiResponse> condition, PollStrategy strategy);

    /**
     * Poll {@code path} until {@code stateOf} returns a terminal value.
     *
     * @param path     the status endpoint, e.g. "/prior-authorizations/{id}"
     * @param stateOf  extracts the state string from each response
     * @param terminal decides whether a given state is terminal
     * @param strategy delay schedule and attempt cap
     * @return the final, terminal response
     */
    ApiResponse pollUntilTerminal(String path,
                                  Function<ApiResponse, String> stateOf,
                                  TerminalStateResolver terminal,
                                  PollStrategy strategy);

    /** Poll {@code path} until {@code condition} holds, using exponential backoff from config. */
    ApiResponse pollWithBackoff(String path, Predicate<ApiResponse> condition);

    String serverName();
}
