package org.openagent4j.execution;

import lombok.Builder;

@Builder(toBuilder = true)
public record RetryPolicy(int maxAttempts, long initialBackoffMs, boolean exponential) {
    public static RetryPolicy exponentialBackoff(int maxAttempts) {
        return new RetryPolicy(maxAttempts, 1000L, true);
    }
}
