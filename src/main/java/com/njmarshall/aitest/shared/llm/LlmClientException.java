package com.njmarshall.aitest.shared.llm;

/** Unchecked failure from the LLM transport or response parsing. */
public class LlmClientException extends RuntimeException {

    public LlmClientException(String message) {
        super(message);
    }

    public LlmClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
