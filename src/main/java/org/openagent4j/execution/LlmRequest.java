package org.openagent4j.execution;

import java.util.List;
import java.util.Objects;
import org.openagent4j.memory.LlmSession;
import org.openagent4j.memory.Memory;
import org.openagent4j.model.Model;
import org.openagent4j.model.ModelConfiguration;
import org.openagent4j.model.ReasoningConfig;
import org.openagent4j.tool.McpTool;
import org.openagent4j.tool.Tool;

/**
 * Everything needed to perform one agent step (system role, task text, tools, routing hints).
 */
public record LlmRequest(
        String agentName,
        String agentAbout,
        String taskPrompt,
        Model model,
        ModelConfiguration modelConfig,
        List<Tool> internalTools,
        List<McpTool> mcpTools,
        Memory memory,
        LlmSession session,
        ReasoningConfig reasoningConfig,
        RetryPolicy retryPolicy,
        Double minConfidence) {

    public LlmRequest {
        Objects.requireNonNull(agentName, "agentName");
        Objects.requireNonNull(agentAbout, "agentAbout");
        Objects.requireNonNull(taskPrompt, "taskPrompt");
        internalTools = internalTools == null ? List.of() : List.copyOf(internalTools);
        mcpTools = mcpTools == null ? List.of() : List.copyOf(mcpTools);
    }

    public String systemMessage() {
        return "You are " + agentName + ". " + agentAbout;
    }
}
