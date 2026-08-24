package com.njmarshall.aitest.shared.client.poll;

/**
 * Grows the delay geometrically and clamps it at a ceiling: delay(attempt) =
 * min(maxDelay, initialDelay * multiplier^(attempt - 1)). This is what long-running adjudication
 * workflows want, because early polls are cheap and frequent while later polls back off to avoid
 * pounding a back end that may take minutes to settle.
 */
public record ExponentialBackoffStrategy(
        long initialDelayMillis,
        long maxDelayMillis,
        double multiplier,
        int maxAttempts) implements PollStrategy {

    public ExponentialBackoffStrategy {
        if (initialDelayMillis < 0) {
            throw new IllegalArgumentException("initialDelayMillis must be >= 0");
        }
        if (maxDelayMillis < initialDelayMillis) {
            throw new IllegalArgumentException("maxDelayMillis must be >= initialDelayMillis");
        }
        if (multiplier < 1.0) {
            throw new IllegalArgumentException("multiplier must be >= 1.0");
        }
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
    }

    @Override
    public long delayMillisFor(int attempt) {
        double raw = initialDelayMillis * Math.pow(multiplier, attempt - 1);
        if (Double.isInfinite(raw) || raw > maxDelayMillis) {
            return maxDelayMillis;
        }
        return (long) raw;
    }
}
