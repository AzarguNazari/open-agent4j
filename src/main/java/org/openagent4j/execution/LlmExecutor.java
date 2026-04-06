package org.openagent4j.execution;

/**
 * Bridges {@link org.openagent4j.LlmAgent} to a concrete LLM or test double. Implementations return the raw text
 * (for structured agents, typically JSON that matches the configured return type).
 */
@FunctionalInterface
public interface LlmExecutor {

    String complete(LlmRequest request);
}
