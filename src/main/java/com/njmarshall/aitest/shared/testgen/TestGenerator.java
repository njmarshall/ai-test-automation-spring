package com.njmarshall.aitest.shared.testgen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.njmarshall.aitest.shared.config.FrameworkProperties;
import com.njmarshall.aitest.shared.llm.LlmClient;

import java.util.List;

/**
 * AI-POWERED TEST GENERATION.
 *
 * <p>Pipeline: load an OpenAPI spec, flatten it to operations, ask the model for a suite of test
 * cases as strict JSON, then deserialize. The generator depends only on the {@link LlmClient}
 * facade and the {@link OpenApiSpecLoader} interface, so it is fully unit-testable with a stub
 * model and no spec file.
 *
 * <p>This is a plain collaborator, not a Spring @Component, but it is declared as a singleton bean
 * in FrameworkConfig because it holds no per-call state. The freshness/quality gate (the DeepEval
 * LLM-as-judge layer from the Python version) belongs downstream of this class: generate here,
 * score before merge there.
 */
public class TestGenerator {

    private static final String SYSTEM_PROMPT = """
            You are a senior SDET generating API test cases from an OpenAPI specification.
            Return ONLY a JSON array. No prose, no markdown fences. Each element must be an object:
            {
              "name": string,
              "method": string,
              "path": string,
              "category": one of "happy-path" | "validation" | "authz" | "boundary" | "negative",
              "description": string,
              "expectedStatus": integer
            }
            Cover happy paths and at least one validation, authz, and boundary case per operation
            where the spec makes them meaningful. Do not invent paths that are not in the input.
            """;

    private final LlmClient llmClient;
    private final OpenApiSpecLoader specLoader;
    private final ObjectMapper objectMapper;
    private final FrameworkProperties.Llm llmConfig;

    public TestGenerator(LlmClient llmClient,
                         OpenApiSpecLoader specLoader,
                         ObjectMapper objectMapper,
                         FrameworkProperties.Llm llmConfig) {
        this.llmClient = llmClient;
        this.specLoader = specLoader;
        this.objectMapper = objectMapper;
        this.llmConfig = llmConfig;
    }

    /** Load a spec and generate cases for every operation in it. */
    public List<GeneratedTestCase> generateFromSpec(String specLocation) {
        List<ApiOperation> operations = specLoader.load(specLocation);
        return generateFromOperations(operations);
    }

    /** Generate cases for an explicit set of operations. Handy for targeted regeneration. */
    public List<GeneratedTestCase> generateFromOperations(List<ApiOperation> operations) {
        if (operations.isEmpty()) {
            return List.of();
        }
        String userPrompt = buildUserPrompt(operations);
        String reply = llmClient.complete(SYSTEM_PROMPT, userPrompt);
        return parse(reply);
    }

    private String buildUserPrompt(List<ApiOperation> operations) {
        try {
            String operationsJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(operations);
            return "Model in use: " + llmConfig.getModel() + "\n\n"
                    + "Generate test cases for these operations:\n" + operationsJson;
        } catch (Exception e) {
            throw new TestGenerationException("Failed to serialize operations for the prompt", e);
        }
    }

    private List<GeneratedTestCase> parse(String reply) {
        String json = stripFences(reply).trim();
        try {
            GeneratedTestCase[] cases = objectMapper.readValue(json, GeneratedTestCase[].class);
            return List.of(cases);
        } catch (Exception e) {
            throw new TestGenerationException("Model did not return a valid JSON array:\n" + reply, e);
        }
    }

    /** Defensive: if the model wrapped the array in a ```json fence despite instructions, remove it. */
    private String stripFences(String text) {
        String t = text.strip();
        if (t.startsWith("```")) {
            int firstNewline = t.indexOf('\n');
            if (firstNewline >= 0) {
                t = t.substring(firstNewline + 1);
            }
            if (t.endsWith("```")) {
                t = t.substring(0, t.length() - 3);
            }
        }
        return t;
    }
}
