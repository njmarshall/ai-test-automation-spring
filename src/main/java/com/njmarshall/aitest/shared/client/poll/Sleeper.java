package com.njmarshall.aitest.shared.client.poll;

/**
 * Wraps the wait between polls so tests can inject a no-op sleeper and assert backoff behavior in
 * microseconds instead of real minutes. In production the default implementation calls
 * {@link Thread#sleep(long)}.
 */
@FunctionalInterface
public interface Sleeper {

    void sleep(long millis);

    /** Real sleeper; restores the interrupt flag and converts to an unchecked failure. */
    Sleeper REAL = millis -> {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PollException("Polling was interrupted while sleeping", e);
        }
    };
}
