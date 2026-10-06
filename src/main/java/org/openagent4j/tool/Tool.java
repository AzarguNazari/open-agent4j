package org.openagent4j.tool;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Declares a tool the agent may use. Execution is handled by the configured {@link org.openagent4j.execution.LlmExecutor}.
 */
public record Tool(
        String name,
        String description,
        Function<ToolArguments, Object> action,
        Consumer<ToolArguments> preValidator) {

    public Tool {
        Objects.requireNonNull(name, "name");
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static Tool of(String name, String description) {
        return builder(name).description(description).build();
    }

    public static final class Builder {
        private final String name;
        private String description;
        private Function<ToolArguments, Object> action;
        private Consumer<ToolArguments> preValidator;

        private Builder(String name) {
            this.name = Objects.requireNonNull(name, "name");
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder action(Function<ToolArguments, Object> action) {
            this.action = action;
            return this;
        }

        public Builder preValidator(Consumer<ToolArguments> preValidator) {
            this.preValidator = preValidator;
            return this;
        }

        public Tool build() {
            return new Tool(name, description, action, preValidator);
        }
    }
}
