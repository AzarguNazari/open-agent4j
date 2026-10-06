package org.openagent4j.execution;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.openagent4j.config.ProviderSettings;
import org.openagent4j.memory.LlmSession;
import org.openagent4j.memory.Memory;
import org.openagent4j.model.Model;
import org.openagent4j.model.ModelConfiguration;
import org.openagent4j.model.ReasoningConfig;
import org.openagent4j.tool.McpTool;
import org.openagent4j.tool.Tool;

public record LlmRequest(
        String agentName,
        String agentAbout,
        String purpose,
        String taskPrompt,
        Model model,
        ModelConfiguration modelConfig,
        List<Tool> tools,
        List<McpTool> mcpTools,
        Memory memory,
        LlmSession session,
        ReasoningConfig reasoningConfig,
        RetryPolicy retryPolicy,
        Double minConfidence,
        Class<?> responseType,
        ProviderSettings providerSettings,
        Consumer<AgentStep> onStep,
        BiConsumer<Tool, Throwable> onToolError) {

    public LlmRequest {
        tools = immutableCopy(tools);
        mcpTools = immutableCopy(mcpTools);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String systemMessage() {
        if (purpose == null) {
            return "";
        }
        return purpose.trim();
    }

    public boolean expectsStructuredResponse() {
        return responseType != null && responseType != String.class;
    }

    private static <E> List<E> immutableCopy(List<E> values) {
        if (values == null) {
            return List.of();
        }
        return List.copyOf(values);
    }

    public static final class Builder {
        private String agentName;
        private String agentAbout;
        private String purpose;
        private String taskPrompt;
        private Model model;
        private ModelConfiguration modelConfig;
        private List<Tool> tools;
        private List<McpTool> mcpTools;
        private Memory memory;
        private LlmSession session;
        private ReasoningConfig reasoningConfig;
        private RetryPolicy retryPolicy;
        private Double minConfidence;
        private Class<?> responseType;
        private ProviderSettings providerSettings;
        private Consumer<AgentStep> onStep;
        private BiConsumer<Tool, Throwable> onToolError;

        private Builder() {}

        public Builder agentName(String agentName) {
            this.agentName = agentName;
            return this;
        }

        public Builder agentAbout(String agentAbout) {
            this.agentAbout = agentAbout;
            return this;
        }

        public Builder purpose(String purpose) {
            this.purpose = purpose;
            return this;
        }

        public Builder taskPrompt(String taskPrompt) {
            this.taskPrompt = taskPrompt;
            return this;
        }

        public Builder model(Model model) {
            this.model = model;
            return this;
        }

        public Builder modelConfig(ModelConfiguration modelConfig) {
            this.modelConfig = modelConfig;
            return this;
        }

        public Builder tools(List<Tool> tools) {
            this.tools = tools;
            return this;
        }

        public Builder mcpTools(List<McpTool> mcpTools) {
            this.mcpTools = mcpTools;
            return this;
        }

        public Builder memory(Memory memory) {
            this.memory = memory;
            return this;
        }

        public Builder session(LlmSession session) {
            this.session = session;
            return this;
        }

        public Builder reasoningConfig(ReasoningConfig reasoningConfig) {
            this.reasoningConfig = reasoningConfig;
            return this;
        }

        public Builder retryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        public Builder minConfidence(Double minConfidence) {
            this.minConfidence = minConfidence;
            return this;
        }

        public Builder responseType(Class<?> responseType) {
            this.responseType = responseType;
            return this;
        }

        public Builder providerSettings(ProviderSettings providerSettings) {
            this.providerSettings = providerSettings;
            return this;
        }

        public Builder onStep(Consumer<AgentStep> onStep) {
            this.onStep = onStep;
            return this;
        }

        public Builder onToolError(BiConsumer<Tool, Throwable> onToolError) {
            this.onToolError = onToolError;
            return this;
        }

        public LlmRequest build() {
            return new LlmRequest(
                    agentName,
                    agentAbout,
                    purpose,
                    taskPrompt,
                    model,
                    modelConfig,
                    tools,
                    mcpTools,
                    memory,
                    session,
                    reasoningConfig,
                    retryPolicy,
                    minConfidence,
                    responseType,
                    providerSettings,
                    onStep,
                    onToolError);
        }
    }
}
