package org.openagent4j.execution;

import java.util.List;
import java.util.Objects;
import org.openagent4j.config.ProviderSettings;
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
        ProviderSettings providerSettings) {

    public LlmRequest {
        Objects.requireNonNull(agentName, "agentName");
        Objects.requireNonNull(agentAbout, "agentAbout");
        Objects.requireNonNull(taskPrompt, "taskPrompt");
        tools = tools == null ? List.of() : List.copyOf(tools);
        mcpTools = mcpTools == null ? List.of() : List.copyOf(mcpTools);
    }

    public String systemMessage() {
        if (purpose == null || purpose.isBlank()) {
            return "";
        }
        return purpose.trim();
    }
}
