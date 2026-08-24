package com.njmarshall.aitest.shared.client.poll;

/** Waits the same amount before every attempt. Good for fast, predictable back ends. */
public record FixedDelayStrategy(long delayMillis, int maxAttempts) implements PollStrategy {

    public FixedDelayStrategy {
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis must be >= 0");
        }
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
    }

    @Override
    public long delayMillisFor(int attempt) {
        return delayMillis;
    }
}
