package com.njmarshall.aitest.shared.testgen;

/** Unchecked failure while generating or parsing AI test cases. */
public class TestGenerationException extends RuntimeException {

    public TestGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
