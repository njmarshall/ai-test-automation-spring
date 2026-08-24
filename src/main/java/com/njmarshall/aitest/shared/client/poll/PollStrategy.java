package com.njmarshall.aitest.shared.client.poll;

/**
 * STRATEGY.
 *
 * <p>The polling loop is fixed. What changes between workflows is how long to wait before the next
 * attempt and how many attempts to allow. Each of those variations is a strategy object, mirroring
 * the TimeoutStrategy / FixedRetryStrategy / ExponentialBackoffStrategy trio from the Python
 * framework's AsyncPoller.
 */
public interface PollStrategy {

    /**
     * Delay before the given attempt.
     *
     * @param attempt 1-based attempt number (1 is the first poll after submission)
     * @return milliseconds to sleep before this attempt
     */
    long delayMillisFor(int attempt);

    /** Maximum number of attempts before the poll is declared failed. */
    int maxAttempts();
}
