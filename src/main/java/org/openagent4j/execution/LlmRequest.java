package org.openagent4j.execution;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.Builder;
import org.openagent4j.config.ProviderSettings;
import org.openagent4j.memory.LlmSession;
import org.openagent4j.memory.Memory;
import org.openagent4j.model.Model;
import org.openagent4j.model.ModelConfiguration;
import org.openagent4j.model.ReasoningConfig;
import org.openagent4j.tool.McpTool;
import org.openagent4j.tool.Tool;

@lombok.Value
@lombok.Builder
@lombok.experimental.Accessors(fluent = true)
public class LlmRequest {
    String agentName;
    String agentAbout;
    String purpose;
    String taskPrompt;
    Model model;
    ModelConfiguration modelConfig;
    List<Tool> tools;
    List<McpTool> mcpTools;
    Memory memory;
    LlmSession session;
    ReasoningConfig reasoningConfig;
    RetryPolicy retryPolicy;
    Double minConfidence;
    Class<?> responseType;
    ProviderSettings providerSettings;
    Consumer<AgentStep> onStep;
    BiConsumer<Tool, Throwable> onToolError;



    public String systemMessage() {
        if (purpose == null) {
            return "";
        }
        if (purpose.isBlank()) {
            return "";
        }
        return purpose.trim();
    }

    public boolean expectsStructuredResponse() {
        if (responseType == null) {
            return false;
        }
        if (responseType == String.class) {
            return false;
        }
        return true;
    }
}
