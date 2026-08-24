package com.njmarshall.aitest.shared.llm;

/**
 * FACADE + STRATEGY.
 *
 * <p>One tiny surface hides everything about the model provider: base URL, headers, request
 * shape, response parsing, retries. The TestGenerator depends only on this interface, so in unit
 * tests we hand it a stub that returns a canned string and never touches the network. Swapping
 * providers (Anthropic, a local model, a record/replay fixture) is a one-line bean change.
 */
public interface LlmClient {

    /**
     * Send one system+user turn and return the model's plain text reply.
     *
     * @param systemPrompt the instruction/role text
     * @param userPrompt   the task/content text
     * @return the concatenated text of the model's response
     */
    String complete(String systemPrompt, String userPrompt);
}
