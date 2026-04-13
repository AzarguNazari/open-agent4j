package org.openagent4j.tool;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Builder;

/**
 * Declares a tool the agent may use. Execution is handled by the configured {@link org.openagent4j.execution.LlmExecutor}.
 */
@Builder(toBuilder = true, builderMethodName = "internalBuilder")
public record Tool(
        String name,
        String description,
        Function<ToolArguments, Object> action,
        Consumer<ToolArguments> preValidator) {

    public Tool {
        Objects.requireNonNull(name, "name");
    }

    public static ToolBuilder builder(String name) {
        return internalBuilder().name(name);
    }

    public static Tool of(String name, String description) {
        return builder(name).description(description).build();
    }
}
