package com.njmarshall.aitest.projects.healthcare.client;

import com.njmarshall.aitest.projects.healthcare.model.PriorAuthStatus;
import com.njmarshall.aitest.projects.healthcare.model.PriorAuthorization;
import com.njmarshall.aitest.shared.client.ApiResponse;
import com.njmarshall.aitest.shared.client.AsyncApiClient;
import com.njmarshall.aitest.shared.client.SyncApiClient;
import com.njmarshall.aitest.shared.client.poll.PollStrategy;

/**
 * FACADE for the prior authorization workflow.
 *
 * <p>Hides the two-step reality of prior auth behind one small API: submit synchronously, then
 * poll asynchronously until the payer returns a decision. Tests read like the business process,
 * not like HTTP plumbing.
 *
 * <p>It depends on the two segregated interfaces separately. That is Interface Segregation paying
 * off in practice: the submit path needs only {@link SyncApiClient}, the wait path needs only
 * {@link AsyncApiClient}.
 */
public class PriorAuthApi {

    private static final String COLLECTION = "/prior-authorizations";

    private final SyncApiClient sync;
    private final AsyncApiClient async;

    public PriorAuthApi(SyncApiClient sync, AsyncApiClient async) {
        this.sync = sync;
        this.async = async;
    }

    /** Submit a new prior authorization. The response body carries the assigned id. */
    public ApiResponse submit(PriorAuthorization request) {
        return sync.post(COLLECTION, request);
    }

    /** Poll the status endpoint with exponential backoff until the decision is terminal. */
    public ApiResponse awaitDecision(String id) {
        return async.pollWithBackoff(
                statusPath(id),
                response -> PriorAuthStatus.isTerminalState(response.field("status")));
    }

    /** Same wait, but with a caller-supplied schedule (e.g. a fast strategy in tests). */
    public ApiResponse awaitDecision(String id, PollStrategy strategy) {
        return async.pollUntilTerminal(
                statusPath(id),
                response -> response.field("status"),
                PriorAuthStatus::isTerminalState,
                strategy);
    }

    private String statusPath(String id) {
        return COLLECTION + "/" + id;
    }
}
