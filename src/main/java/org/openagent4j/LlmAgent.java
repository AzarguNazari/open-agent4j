package org.openagent4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.openagent4j.config.OpenAgentProperties;
import org.openagent4j.config.ProviderSettings;
import org.openagent4j.execution.AgentStep;
import org.openagent4j.execution.LlmExecutor;
import org.openagent4j.execution.LlmRequest;
import org.openagent4j.execution.RetryPolicy;
import org.openagent4j.memory.LlmSession;
import org.openagent4j.memory.Memory;
import org.openagent4j.model.Model;
import org.openagent4j.model.ModelConfiguration;
import org.openagent4j.model.ReasoningConfig;
import org.openagent4j.tool.McpTool;
import org.openagent4j.tool.ServiceTools;
import org.openagent4j.tool.Tool;

/**
 * Configurable LLM-backed agent: prompt template, typed JSON result, tools, MCP references, model choice, and execution hook.
 * {@code onToolError} is reserved for executors that invoke tools; {@link org.openagent4j.execution.OpenAiCompatibleLlmExecutor} does not
 * call tools yet.
 */
public record LlmAgent<T>(
        String name,
        String about,
        String purpose,
        String task,
        Class<T> returnType,
        List<Tool> tools,
        List<McpTool> mcpTools,
        Double minConfidence,
        Memory memory,
        LlmSession session,
        Model model,
        ModelConfiguration modelConfig,
        ReasoningConfig reasoningConfig,
        RetryPolicy retryPolicy,
        Consumer<AgentStep> onStep,
        BiConsumer<Tool, Throwable> onToolError,
        OpenAgentProperties agentProperties,
        LlmExecutor llmExecutor) {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    public LlmAgent {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(about, "about");
        Objects.requireNonNull(task, "task");
        Objects.requireNonNull(llmExecutor, "llmExecutor");
        Objects.requireNonNull(agentProperties, "agentProperties");
        tools = tools == null ? List.of() : List.copyOf(tools);
        mcpTools = mcpTools == null ? List.of() : List.copyOf(mcpTools);
    }

    public static Builder<Object> builder() {
        return new Builder<>();
    }

    public static <R> Builder<R> builder(Class<R> returnType) {
        Builder<R> builder = new Builder<>();
        builder.returnType = returnType;
        return builder;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static List<Tool> tools(Tool first, Tool... more) {
        Objects.requireNonNull(first, "first");
        if (more == null || more.length == 0) {
            return List.of(first);
        }
        List<Tool> list = new ArrayList<>(1 + more.length);
        list.add(first);
        Collections.addAll(list, more);
        return List.copyOf(list);
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static List<McpTool> mcps(McpTool first, McpTool... more) {
        Objects.requireNonNull(first, "first");
        if (more == null || more.length == 0) {
            return List.of(first);
        }
        List<McpTool> list = new ArrayList<>(1 + more.length);
        list.add(first);
        Collections.addAll(list, more);
        return List.copyOf(list);
    }

    @SuppressWarnings("unchecked")
    public T run() {
        return run("");
    }

    /**
     * Runs the agent: substitutes {@code {input}} in {@code task}, calls {@link LlmExecutor#complete(LlmRequest)}, then parses JSON
     * into {@code returnType} when set (otherwise returns the raw string).
     */
    @SuppressWarnings("unchecked")
    public T run(String input) {
        Objects.requireNonNull(input, "input");
        String taskPrompt = task.replace("{input}", input);
        ProviderSettings providerSettings = agentProperties.resolve(model);
        LlmRequest request = new LlmRequest(
                name,
                about,
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
                returnType,
                providerSettings);

        if (onStep != null) {
            onStep.accept(new AgentStep("Starting execution"));
        }

        String rawText = llmExecutor.complete(request);
        if (returnType == null || returnType == String.class) {
            return (T) rawText;
        }
        try {
            return OBJECT_MAPPER.readValue(rawText, returnType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse model output as " + returnType.getName(), e);
        }
    }

    public CompletableFuture<T> runAsync() {
        return CompletableFuture.supplyAsync(this::run);
    }

    public CompletableFuture<T> runAsync(String input) {
        return CompletableFuture.supplyAsync(() -> run(input));
    }

    public static final class Builder<T> {

        private String name;
        private String about;
        private String purpose;
        private String task;
        private Class<?> returnType;
        private final List<Tool> tools = new ArrayList<>();
        private final List<McpTool> mcpTools = new ArrayList<>();
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

        @SuppressWarnings("unchecked")
        public <R> Builder<R> response(Class<R> returnType) {
            this.returnType = returnType;
            return (Builder<R>) (Builder<?>) this;
        }

        public Builder<T> tools(Tool tool) {
            Objects.requireNonNull(tool, "tool");
            tools.add(tool);
            return this;
        }

        public Builder<T> tools(Tool first, Tool... more) {
            tools.add(Objects.requireNonNull(first, "first"));
            if (more != null) {
                Collections.addAll(tools, more);
            }
            return this;
        }

        public Builder<T> tools(List<Tool> toolList) {
            Objects.requireNonNull(toolList, "toolList");
            tools.addAll(toolList);
            return this;
        }

        /**
         * Registers tools from a {@link Tool} instance or from {@link org.openagent4j.tool.AgentTool}-annotated methods.
         */
        public Builder<T> tools(Object toolOrService) {
            Objects.requireNonNull(toolOrService, "toolOrService");
            if (toolOrService instanceof Tool t) {
                tools.add(t);
                return this;
            }
            tools.addAll(ServiceTools.fromObject(toolOrService));
            return this;
        }

        public Builder<T> mcp(McpTool mcpTool) {
            Objects.requireNonNull(mcpTool, "mcpTool");
            mcpTools.add(mcpTool);
            return this;
        }

        public Builder<T> mcps(List<McpTool> mcps) {
            Objects.requireNonNull(mcps, "mcps");
            mcpTools.addAll(mcps);
            return this;
        }

        public Builder<T> mcps(McpTool first, McpTool... more) {
            mcpTools.add(Objects.requireNonNull(first, "first"));
            if (more != null) {
                Collections.addAll(mcpTools, more);
            }
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

        @SuppressWarnings("unchecked")
        public LlmAgent<T> build() {
            OpenAgentProperties resolvedProps = agentProperties != null ? agentProperties : OpenAgentProperties.load();
            return new LlmAgent<>(
                    name,
                    about,
                    purpose,
                    task,
                    (Class<T>) returnType,
                    List.copyOf(tools),
                    List.copyOf(mcpTools),
                    minConfidence,
                    memory,
                    session,
                    model,
                    modelConfig,
                    reasoningConfig,
                    retryPolicy,
                    onStep,
                    onToolError,
                    resolvedProps,
                    llmExecutor);
        }
    }
}
