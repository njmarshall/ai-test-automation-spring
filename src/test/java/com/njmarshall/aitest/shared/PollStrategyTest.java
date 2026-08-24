package com.njmarshall.aitest.shared;

import com.njmarshall.aitest.shared.client.poll.ExponentialBackoffStrategy;
import com.njmarshall.aitest.shared.client.poll.FixedDelayStrategy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PollStrategyTest {

    @Test
    void fixedDelayIsConstant() {
        FixedDelayStrategy strategy = new FixedDelayStrategy(250, 5);

        assertThat(strategy.maxAttempts()).isEqualTo(5);
        assertThat(strategy.delayMillisFor(1)).isEqualTo(250);
        assertThat(strategy.delayMillisFor(5)).isEqualTo(250);
    }

    @Test
    void exponentialBackoffGrowsThenClampsAtCeiling() {
        ExponentialBackoffStrategy strategy =
                new ExponentialBackoffStrategy(100, 1000, 2.0, 10);

        assertThat(strategy.delayMillisFor(1)).isEqualTo(100);   // 100 * 2^0
        assertThat(strategy.delayMillisFor(2)).isEqualTo(200);   // 100 * 2^1
        assertThat(strategy.delayMillisFor(3)).isEqualTo(400);   // 100 * 2^2
        assertThat(strategy.delayMillisFor(4)).isEqualTo(800);   // 100 * 2^3
        assertThat(strategy.delayMillisFor(5)).isEqualTo(1000);  // 1600 clamped to 1000
        assertThat(strategy.delayMillisFor(9)).isEqualTo(1000);  // stays clamped
    }

    @Test
    void constructorsRejectNonsenseArguments() {
        assertThatThrownBy(() -> new FixedDelayStrategy(-1, 5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FixedDelayStrategy(100, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExponentialBackoffStrategy(100, 50, 2.0, 5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExponentialBackoffStrategy(100, 1000, 0.5, 5))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
