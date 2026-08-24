package com.njmarshall.aitest.projects.insurance.client;

import com.njmarshall.aitest.projects.insurance.model.Claim;
import com.njmarshall.aitest.projects.insurance.model.ClaimStatus;
import com.njmarshall.aitest.shared.client.ApiResponse;
import com.njmarshall.aitest.shared.client.AsyncApiClient;
import com.njmarshall.aitest.shared.client.SyncApiClient;
import com.njmarshall.aitest.shared.client.poll.PollStrategy;

/**
 * FACADE for the claims adjudication workflow: submit a claim, then poll until the payer settles
 * it (PAID / DENIED / REJECTED). Structurally identical to the prior auth facade, which is the
 * point: the shared client and poller carry the workflow, each domain just names its own paths and
 * terminal states.
 */
public class ClaimApi {

    private static final String COLLECTION = "/claims";

    private final SyncApiClient sync;
    private final AsyncApiClient async;

    public ClaimApi(SyncApiClient sync, AsyncApiClient async) {
        this.sync = sync;
        this.async = async;
    }

    public ApiResponse submit(Claim request) {
        return sync.post(COLLECTION, request);
    }

    public ApiResponse awaitAdjudication(String id) {
        return async.pollWithBackoff(
                statusPath(id),
                response -> ClaimStatus.isTerminalState(response.field("status")));
    }

    public ApiResponse awaitAdjudication(String id, PollStrategy strategy) {
        return async.pollUntilTerminal(
                statusPath(id),
                response -> response.field("status"),
                ClaimStatus::isTerminalState,
                strategy);
    }

    private String statusPath(String id) {
        return COLLECTION + "/" + id;
    }
}
