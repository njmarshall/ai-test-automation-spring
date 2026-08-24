package com.njmarshall.aitest.shared.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.njmarshall.aitest.shared.config.FrameworkProperties;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Concrete {@link LlmClient} backed by the Anthropic Messages API and Spring's {@link RestClient}.
 *
 * <p>RestClient is the synchronous HTTP client introduced in Spring 6.1. It replaces the
 * RestAssured request builder used in the Java version. We do not need RestAssured's assertion
 * DSL here, because assertions live in our own CRTP validators.
 */
public class AnthropicLlmClient implements LlmClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final FrameworkProperties.Llm config;

    public AnthropicLlmClient(FrameworkProperties.Llm config,
                              RestClient.Builder builder,
                              ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        String apiKey = resolveApiKey(config);
        this.restClient = builder
                .baseUrl(config.getBaseUrl())
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", config.getAnthropicVersion())
                .defaultHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        Map<String, Object> body = Map.of(
                "model", config.getModel(),
                "max_tokens", config.getMaxTokens(),
                "system", systemPrompt,
                "messages", List.of(Map.of("role", "user", "content", userPrompt))
        );

        String raw = restClient.post()
                .uri("/v1/messages")
                .body(body)
                .retrieve()
                .body(String.class);

        return extractText(raw);
    }

    /**
     * The response is a JSON object whose "content" is an array of blocks. We concatenate the
     * text of every block of type "text". Parsing by block type, not by array position, keeps us
     * correct even when the model returns multiple blocks.
     */
    private String extractText(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode content = root.path("content");
            StringBuilder sb = new StringBuilder();
            if (content.isArray()) {
                for (JsonNode block : content) {
                    if ("text".equals(block.path("type").asText())) {
                        sb.append(block.path("text").asText());
                    }
                }
            }
            return sb.toString();
        } catch (Exception e) {
            throw new LlmClientException("Failed to parse LLM response: " + raw, e);
        }
    }

    private static String resolveApiKey(FrameworkProperties.Llm config) {
        if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
            return config.getApiKey();
        }
        String fromEnv = System.getenv("AITEST_LLM_API_KEY");
        if (fromEnv == null || fromEnv.isBlank()) {
            throw new LlmClientException(
                    "No LLM API key. Set aitest.llm.api-key or the AITEST_LLM_API_KEY environment variable.");
        }
        return fromEnv;
    }
}
