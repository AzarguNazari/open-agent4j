package org.openagent4j.tool;

import java.util.Objects;

/**
 * Declares a tool the agent may use. Execution is handled by the configured {@link org.openagent4j.execution.LlmExecutor}.
 */
public record Tool(String name, String description) {

    public Tool {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
    }

    public static Tool of(String name, String description) {
        return new Tool(name, description);
    }
}
