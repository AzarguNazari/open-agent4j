package org.openagent4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Singular;
import org.openagent4j.execution.LlmExecutor;
import org.openagent4j.execution.LlmRequest;
import org.openagent4j.memory.Memory;
import org.openagent4j.model.Model;
import org.openagent4j.model.ModelConfiguration;
import org.openagent4j.tool.McpTool;
import org.openagent4j.tool.Tool;

/**
 * Configurable LLM-backed agent: prompt template, typed JSON result, tools, MCP references, model choice, and execution hook.
 */
@Builder(toBuilder = true)
public record LlmAgent(
        String name,
        String about,
        String task,
        Class<?> returnType,
        @Singular("internalTool") List<Tool> internalTools,
        @Singular("mcp") List<McpTool> mcpTools,
        Double minConfidence,
        Memory memory,
        Model model,
        ModelConfiguration modelConfig,
        LlmExecutor llmExecutor) {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    /**
     * Vararg-friendly list for the generated builder's {@code internalTools(Collection)} so call sites can batch tools in one call.
     */
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

    /** Vararg-friendly list for the generated builder's {@code mcpTools(Collection)}. */
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

    public LlmAgent {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(about, "about");
        Objects.requireNonNull(task, "task");
        Objects.requireNonNull(llmExecutor, "llmExecutor");
        internalTools = internalTools == null ? List.of() : List.copyOf(internalTools);
        mcpTools = mcpTools == null ? List.of() : List.copyOf(mcpTools);
    }

    /**
     * Runs the agent: substitutes {@code {input}} in {@code task}, calls {@link LlmExecutor#complete(LlmRequest)}, then parses JSON
     * into {@code returnType} when set (otherwise returns the raw string).
     */
    public Object run(String input) {
        Objects.requireNonNull(input, "input");
        String taskPrompt = task.replace("{input}", input);
        LlmRequest request = new LlmRequest(
                name,
                about,
                taskPrompt,
                model,
                modelConfig,
                internalTools,
                mcpTools,
                memory,
                minConfidence);
        String rawText = llmExecutor.complete(request);
        if (returnType == null || returnType == String.class) {
            return rawText;
        }
        try {
            return OBJECT_MAPPER.readValue(rawText, returnType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse model output as " + returnType.getName(), e);
        }
    }
}
