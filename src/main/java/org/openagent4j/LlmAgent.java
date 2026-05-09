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

@lombok.Value
@lombok.Builder
@lombok.experimental.Accessors(fluent = true)
public class LlmAgent<T> {
    String name;
    String about;
    String purpose;
    String task;
    Class<T> returnType;
    List<Tool> tools;
    List<McpTool> mcpTools;
    Double minConfidence;
    Memory memory;
    LlmSession session;
    Model model;
    ModelConfiguration modelConfig;
    ReasoningConfig reasoningConfig;
    RetryPolicy retryPolicy;
    Consumer<AgentStep> onStep;
    BiConsumer<Tool, Throwable> onToolError;
    OpenAgentProperties agentProperties;
    LlmExecutor llmExecutor;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    @SuppressWarnings("unchecked")
    public T run() {
        return run("");
    }

    @SuppressWarnings("unchecked")
    public T run(String input) {
        Objects.requireNonNull(input, "input");
        String taskPrompt = task.replace("{input}", input);

        OpenAgentProperties resolvedProps = agentProperties;
        if (resolvedProps == null) {
            resolvedProps = OpenAgentProperties.load();
        }

        ProviderSettings providerSettings = resolvedProps.resolve(model);

        List<Tool> finalTools = tools;
        if (finalTools == null) {
            finalTools = List.of();
        } else {
            finalTools = List.copyOf(finalTools);
        }

        List<McpTool> finalMcpTools = mcpTools;
        if (finalMcpTools == null) {
            finalMcpTools = List.of();
        } else {
            finalMcpTools = List.copyOf(finalMcpTools);
        }

        LlmRequest request = LlmRequest.builder()
                .agentName(name)
                .agentAbout(about)
                .purpose(purpose)
                .taskPrompt(taskPrompt)
                .model(model)
                .modelConfig(modelConfig)
                .tools(finalTools)
                .mcpTools(finalMcpTools)
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

        if (onStep != null) {
            onStep.accept(new AgentStep("Starting execution"));
        }

        String rawText = llmExecutor.complete(request);
        if (returnType == null) {
            return (T) rawText;
        }
        if (returnType == String.class) {
            return (T) rawText;
        }
        try {
            String jsonText = stripMarkdownCodeBlocks(rawText);
            return OBJECT_MAPPER.readValue(jsonText, returnType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse model output as " + returnType.getName(), e);
        }
    }

    private static String stripMarkdownCodeBlocks(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline != -1) {
                trimmed = trimmed.substring(firstNewline + 1).trim();
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
            }
        }
        return trimmed;
    }

    public CompletableFuture<T> runAsync() {
        return CompletableFuture.supplyAsync(this::run);
    }

    public CompletableFuture<T> runAsync(String input) {
        return CompletableFuture.supplyAsync(() -> run(input));
    }
}
