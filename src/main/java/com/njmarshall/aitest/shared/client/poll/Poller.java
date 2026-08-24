package com.njmarshall.aitest.shared.client.poll;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The poll loop, separated from the HTTP transport. It repeatedly runs a fetch, checks a
 * condition, and sleeps according to a {@link PollStrategy} until the condition holds or the
 * attempts run out. Because the fetch is a plain {@link Supplier}, this is fully unit-testable
 * with a scripted sequence and a no-op {@link Sleeper}, and it can poll anything, not just HTTP.
 *
 * <p>This is the direct analogue of the shared AsyncPoller in the Python framework.
 */
public class Poller {

    private final Sleeper sleeper;

    public Poller(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    public <T> T pollUntil(Supplier<T> fetch, Predicate<T> condition, PollStrategy strategy) {
        T last = null;
        for (int attempt = 1; attempt <= strategy.maxAttempts(); attempt++) {
            sleeper.sleep(strategy.delayMillisFor(attempt));
            last = fetch.get();
            if (condition.test(last)) {
                return last;
            }
        }
        throw new PollException(
                "Condition not met after " + strategy.maxAttempts() + " attempts. Last value: " + last);
    }
}
