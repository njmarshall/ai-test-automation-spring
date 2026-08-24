package com.njmarshall.aitest.shared.client.poll;

/** Thrown when a poll exhausts its attempts without the condition or terminal state being met. */
public class PollException extends RuntimeException {

    public PollException(String message) {
        super(message);
    }

    public PollException(String message, Throwable cause) {
        super(message, cause);
    }
}
