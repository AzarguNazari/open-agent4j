package org.openagent4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.openagent4j.config.OpenAgentProperties;
import org.openagent4j.config.ProviderSettings;
import org.openagent4j.execution.AgentStep;
import org.openagent4j.execution.LlmExecutor;
import org.openagent4j.execution.LlmRequest;
import org.openagent4j.execution.OpenAiCompatibleLlmExecutor;
import org.openagent4j.execution.RetryPolicy;
import org.openagent4j.memory.LlmSession;
import org.openagent4j.memory.Memory;
import org.openagent4j.model.Model;
import org.openagent4j.model.ModelConfiguration;
import org.openagent4j.model.ReasoningConfig;
import org.openagent4j.response.ResponseParser;
import org.openagent4j.tool.McpTool;
import org.openagent4j.tool.Tool;

public final class LlmAgent<T> {

    private static final String INPUT_PLACEHOLDER = "{input}";

    private final String name;
    private final String about;
    private final String purpose;
    private final String task;
    private final List<Tool> tools;
    private final List<McpTool> mcpTools;
    private final Double minConfidence;
    private final Memory memory;
    private final LlmSession session;
    private final Model model;
    private final ModelConfiguration modelConfig;
    private final ReasoningConfig reasoningConfig;
    private final RetryPolicy retryPolicy;
    private final Consumer<AgentStep> onStep;
    private final BiConsumer<Tool, Throwable> onToolError;
    private final Class<T> returnType;
    private final OpenAgentProperties agentProperties;
    private final LlmExecutor llmExecutor;
    private final ResponseParser<T> responseParser;

    private LlmAgent(Builder<T> builder) {
        this.name = builder.name;
        this.about = builder.about;
        this.purpose = builder.purpose;
        this.task = Objects.requireNonNull(builder.task, "task");
        this.returnType = builder.returnType;
        this.tools = immutableCopy(builder.tools);
        this.mcpTools = immutableCopy(builder.mcpTools);
        this.minConfidence = builder.minConfidence;
        this.memory = builder.memory;
        this.session = builder.session;
        this.model = Objects.requireNonNull(builder.model, "model");
        this.modelConfig = builder.modelConfig;
        this.reasoningConfig = builder.reasoningConfig;
        this.retryPolicy = builder.retryPolicy;
        this.onStep = builder.onStep;
        this.onToolError = builder.onToolError;
        this.agentProperties = valueOrDefault(builder.agentProperties, OpenAgentProperties::load);
        this.llmExecutor = valueOrDefault(builder.llmExecutor, OpenAiCompatibleLlmExecutor::new);
        this.responseParser = valueOrDefault(builder.responseParser, () -> ResponseParser.forType(builder.returnType));
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public T run() {
        return run("");
    }

    public T run(String input) {
        Objects.requireNonNull(input, "input");
        LlmRequest request = buildRequest(task.replace(INPUT_PLACEHOLDER, input));
        notifyStep("Starting execution");
        return responseParser.parse(llmExecutor.complete(request));
    }

    public CompletableFuture<T> runAsync() {
        return CompletableFuture.supplyAsync(this::run);
    }

    public CompletableFuture<T> runAsync(String input) {
        return CompletableFuture.supplyAsync(() -> run(input));
    }

    private LlmRequest buildRequest(String taskPrompt) {
        ProviderSettings providerSettings = agentProperties.resolve(model);
        return LlmRequest.builder()
                .agentName(name)
                .agentAbout(about)
                .purpose(purpose)
                .taskPrompt(taskPrompt)
                .model(model)
                .modelConfig(modelConfig)
                .tools(tools)
                .mcpTools(mcpTools)
                .memory(memory)
                .session(session)
                .reasoningConfig(reasoningConfig)
                .retryPolicy(retryPolicy)
                .minConfidence(minConfidence)
                .responseType(returnType)
                .providerSettings(providerSettings)
                .onStep(onStep)
                .onToolError(onToolError)
                .build();
    }

    private void notifyStep(String action) {
        if (onStep == null) {
            return;
        }
        onStep.accept(new AgentStep(action));
    }

    private static <E> List<E> immutableCopy(List<E> values) {
        if (values == null) {
            return List.of();
        }
        return List.copyOf(values);
    }

    private static <V> V valueOrDefault(V value, Supplier<V> defaultSupplier) {
        if (value == null) {
            return defaultSupplier.get();
        }
        return value;
    }

    public static final class Builder<T> {
        private String name;
        private String about;
        private String purpose;
        private String task;
        private Class<T> returnType;
        private List<Tool> tools;
        private List<McpTool> mcpTools;
        private Double minConfidence;
        private Memory memory;
        private LlmSession session;
        private Model model;
        private ModelConfiguration modelConfig;
        private ReasoningConfig reasoningConfig;
        private RetryPolicy retryPolicy;
        private Consumer<AgentStep> onStep;
        private BiConsumer<Tool, Throwable> onToolError;
        private OpenAgentProperties agentProperties;
        private LlmExecutor llmExecutor;
        private ResponseParser<T> responseParser;

        private Builder() {}

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> about(String about) {
            this.about = about;
            return this;
        }

        public Builder<T> purpose(String purpose) {
            this.purpose = purpose;
            return this;
        }

        public Builder<T> task(String task) {
            this.task = task;
            return this;
        }

        public Builder<T> returnType(Class<T> returnType) {
            this.returnType = returnType;
            return this;
        }

        public Builder<T> tools(List<Tool> tools) {
            this.tools = tools;
            return this;
        }

        public Builder<T> mcpTools(List<McpTool> mcpTools) {
            this.mcpTools = mcpTools;
            return this;
        }

        public Builder<T> minConfidence(Double minConfidence) {
            this.minConfidence = minConfidence;
            return this;
        }

        public Builder<T> memory(Memory memory) {
            this.memory = memory;
            return this;
        }

        public Builder<T> session(LlmSession session) {
            this.session = session;
            return this;
        }

        public Builder<T> model(Model model) {
            this.model = model;
            return this;
        }

        public Builder<T> modelConfig(ModelConfiguration modelConfig) {
            this.modelConfig = modelConfig;
            return this;
        }

        public Builder<T> reasoningConfig(ReasoningConfig reasoningConfig) {
            this.reasoningConfig = reasoningConfig;
            return this;
        }

        public Builder<T> retryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        public Builder<T> onStep(Consumer<AgentStep> onStep) {
            this.onStep = onStep;
            return this;
        }

        public Builder<T> onToolError(BiConsumer<Tool, Throwable> onToolError) {
            this.onToolError = onToolError;
            return this;
        }

        public Builder<T> agentProperties(OpenAgentProperties agentProperties) {
            this.agentProperties = agentProperties;
            return this;
        }

        public Builder<T> llmExecutor(LlmExecutor llmExecutor) {
            this.llmExecutor = llmExecutor;
            return this;
        }

        public Builder<T> responseParser(ResponseParser<T> responseParser) {
            this.responseParser = responseParser;
            return this;
        }

        public LlmAgent<T> build() {
            return new LlmAgent<>(this);
        }
    }
}
