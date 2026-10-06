package org.openagent4j.execution;

public record RetryPolicy(int maxAttempts, long initialBackoffMs, boolean exponential) {

    private static final long DEFAULT_INITIAL_BACKOFF_MS = 1000L;

    public static RetryPolicy exponentialBackoff(int maxAttempts) {
        return new RetryPolicy(maxAttempts, DEFAULT_INITIAL_BACKOFF_MS, true);
    }
}
