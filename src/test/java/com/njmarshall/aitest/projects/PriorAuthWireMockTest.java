package com.njmarshall.aitest.projects;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.njmarshall.aitest.projects.healthcare.client.PriorAuthApi;
import com.njmarshall.aitest.projects.healthcare.factory.PriorAuthFactory;
import com.njmarshall.aitest.shared.client.ApiClientFactory;
import com.njmarshall.aitest.shared.client.ApiResponse;
import com.njmarshall.aitest.shared.client.poll.FixedDelayStrategy;
import com.njmarshall.aitest.shared.client.poll.PollException;
import com.njmarshall.aitest.shared.config.FrameworkProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The keystone integration test: it drives the async polling against a REAL HTTP server, not an in
 * memory fake. WireMock stands up a local server on a random port, returns scripted responses, and
 * records the requests the client actually sent. This is what proves {@code pollUntilTerminal}
 * works end to end over HTTP, and it is where Spring's RestClient is genuinely exercised.
 *
 * <p>The workflow under test mirrors real prior authorization: submit, then poll a status endpoint
 * while the payer moves the request RECEIVED, then IN_REVIEW, then APPROVED.
 */
class PriorAuthWireMockTest {

    private static final String COLLECTION = "/prior-authorizations";
    private static final String STATUS_PATH = COLLECTION + "/pa-1";

    /**
     * Starts and stops a WireMock server around each test. dynamicPort() avoids port clashes on
     * shared CI machines.
     */
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    /** Build a PriorAuthApi whose client points at the running WireMock server. */
    private PriorAuthApi apiPointingAtWireMock() {
        FrameworkProperties.Server server = new FrameworkProperties.Server();
        server.setName("healthcare");
        server.setBaseUrl(wm.baseUrl());

        ApiClientFactory factory = new ApiClientFactory(
                List.of(server),
                new FrameworkProperties.Poll(),
                RestClient.builder());

        return new PriorAuthApi(factory.sync("healthcare"), factory.async("healthcare"));
    }

    @Test
    void submitThenPollReachesApprovedOverRealHttp() {
        // Submit returns a created resource with an id.
        wm.stubFor(post(urlEqualTo(COLLECTION))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"pa-1\"}")));

        // Status endpoint advances one step per poll, using a WireMock scenario as a state machine.
        wm.stubFor(get(urlEqualTo(STATUS_PATH))
                .inScenario("adjudication")
                .whenScenarioStateIs(STARTED)
                .willReturn(okJson("{\"status\":\"RECEIVED\"}"))
                .willSetStateTo("in_review"));
        wm.stubFor(get(urlEqualTo(STATUS_PATH))
                .inScenario("adjudication")
                .whenScenarioStateIs("in_review")
                .willReturn(okJson("{\"status\":\"IN_REVIEW\"}"))
                .willSetStateTo("decided"));
        wm.stubFor(get(urlEqualTo(STATUS_PATH))
                .inScenario("adjudication")
                .whenScenarioStateIs("decided")
                .willReturn(okJson("{\"status\":\"APPROVED\"}")));

        PriorAuthApi api = apiPointingAtWireMock();

        ApiResponse submit = api.submit(new PriorAuthFactory().build());
        assertThat(submit.statusCode()).isEqualTo(201);
        String id = submit.field("id");
        assertThat(id).isEqualTo("pa-1");

        // Zero delay keeps the test instant; up to 5 attempts is ample for three states.
        ApiResponse decision = api.awaitDecision(id, new FixedDelayStrategy(0, 5));

        assertThat(decision.field("status")).isEqualTo("APPROVED");

        // The client really made three GETs: RECEIVED, IN_REVIEW, APPROVED.
        wm.verify(exactly(3), getRequestedFor(urlEqualTo(STATUS_PATH)));
    }

    @Test
    void pollFailsWhenDecisionNeverSettles() {
        // Status never leaves a non terminal state.
        wm.stubFor(get(urlEqualTo(STATUS_PATH))
                .willReturn(okJson("{\"status\":\"IN_REVIEW\"}")));

        PriorAuthApi api = apiPointingAtWireMock();

        assertThatThrownBy(() -> api.awaitDecision("pa-1", new FixedDelayStrategy(0, 3)))
                .isInstanceOf(PollException.class);

        // It exhausted exactly the attempt budget before giving up.
        wm.verify(exactly(3), getRequestedFor(urlEqualTo(STATUS_PATH)));
    }
}
