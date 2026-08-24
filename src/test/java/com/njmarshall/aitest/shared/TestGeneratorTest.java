package com.njmarshall.aitest.shared;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.njmarshall.aitest.shared.config.FrameworkProperties;
import com.njmarshall.aitest.shared.llm.LlmClient;
import com.njmarshall.aitest.shared.testgen.ApiOperation;
import com.njmarshall.aitest.shared.testgen.GeneratedTestCase;
import com.njmarshall.aitest.shared.testgen.OpenApiSpecLoader;
import com.njmarshall.aitest.shared.testgen.TestGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the AI test-generation pipeline with a stub model, so it is deterministic and offline.
 * The facade seam (LlmClient) is exactly what makes this possible.
 */
class TestGeneratorTest {

    private static final String CANNED_JSON = """
            [
              {"name":"submit valid PA returns 201","method":"POST","path":"/prior-authorizations",
               "category":"happy-path","description":"valid body is accepted","expectedStatus":201},
              {"name":"submit missing memberId returns 400","method":"POST","path":"/prior-authorizations",
               "category":"validation","description":"missing required field is rejected","expectedStatus":400}
            ]
            """;

    private TestGenerator generatorReturning(String cannedReply) {
        LlmClient stubLlm = (system, user) -> cannedReply;
        OpenApiSpecLoader stubLoader = location -> List.of(
                new ApiOperation("POST", "/prior-authorizations", "submitPriorAuthorization",
                        "Submit a PA", List.of(), true));
        return new TestGenerator(stubLlm, stubLoader, new ObjectMapper(), new FrameworkProperties.Llm());
    }

    @Test
    void parsesGeneratedCasesFromModelJson() {
        List<GeneratedTestCase> cases = generatorReturning(CANNED_JSON)
                .generateFromSpec("classpath:openapi/prior-auth.yaml");

        assertThat(cases).hasSize(2);
        assertThat(cases.get(0).expectedStatus()).isEqualTo(201);
        assertThat(cases.get(1).category()).isEqualTo("validation");
    }

    @Test
    void toleratesAModelThatWrapsJsonInAMarkdownFence() {
        String fenced = "```json\n" + CANNED_JSON + "\n```";

        List<GeneratedTestCase> cases = generatorReturning(fenced)
                .generateFromSpec("classpath:openapi/prior-auth.yaml");

        assertThat(cases).hasSize(2);
    }

    @Test
    void skipsTheModelCallWhenThereAreNoOperations() {
        LlmClient failIfCalled = (system, user) -> {
            throw new AssertionError("LLM must not be called for an empty operation set");
        };
        TestGenerator generator = new TestGenerator(
                failIfCalled, location -> List.of(), new ObjectMapper(), new FrameworkProperties.Llm());

        assertThat(generator.generateFromOperations(List.of())).isEmpty();
    }
}
