package org.openagent4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openagent4j.execution.LlmRequest;
import org.openagent4j.memory.Memory;
import org.openagent4j.model.Model;
import org.openagent4j.model.ModelConfiguration;
import org.openagent4j.tool.McpTool;
import org.openagent4j.tool.Tool;

class LlmAgentTest {

    record WeatherResponse(String city, String temperature, String nextDayPrediction) {}

    @Test
    void runBuildsRequestInterpolatesInputAndDeserializesReturnType() {
        String task = """
                {input}
                """;

        LlmAgent weatherAgent = LlmAgent.builder()
                .name("Weather Agent")
                .about(
                        "Your are a weather agent and your task is to make sure to find the weather of a specific locaiton which is passed")
                .task(task)
                .returnType(WeatherResponse.class)
                .internalTools(
                        LlmAgent.tools(
                                Tool.of("lookup", "Resolve coordinates"), Tool.of("format", "Format report")))
                .mcpTools(
                        LlmAgent.mcps(
                                McpTool.of("weather-mcp", "current"),
                                McpTool.of("weather-mcp", "forecast")))
                .minConfidence(0.85)
                .memory(Memory.from("session-1", "ttl", "1h"))
                .model(Model.of("acme", "gpt-mock"))
                .modelConfig(ModelConfiguration.temperature(0).maxTokenOutput(500))
                .llmExecutor(req -> {
                    assertEquals("Weather Agent", req.agentName());
                    assertTrue(req.systemMessage().contains("Weather Agent"));
                    assertEquals(
                            """
                            Oslo
                            """.trim(),
                            req.taskPrompt().trim());
                    assertEquals(2, req.internalTools().size());
                    assertEquals(2, req.mcpTools().size());
                    assertEquals(0.85, req.minConfidence());
                    assertEquals("session-1", req.memory().storeId());
                    assertEquals("acme", req.model().provider());
                    assertEquals(0.0, req.modelConfig().temperature());
                    assertEquals(500, req.modelConfig().maxTokenOutput());
                    return """
                            {"city":"Oslo","temperature":"5C","nextDayPrediction":"rain"}
                            """;
                })
                .build();

        Object result = weatherAgent.run("Oslo");
        WeatherResponse typed = assertInstanceOf(WeatherResponse.class, result);
        assertEquals("Oslo", typed.city());
        assertEquals("5C", typed.temperature());
        assertEquals("rain", typed.nextDayPrediction());
    }

    @Test
    void runReturnsRawStringWhenReturnTypeIsStringClass() {
        LlmAgent agent = LlmAgent.builder()
                .name("Echo")
                .about("Echoes output.")
                .task("Say: {input}")
                .returnType(String.class)
                .model(Model.of("test", "model"))
                .modelConfig(ModelConfiguration.temperature(0).maxTokenOutput(10))
                .llmExecutor(LlmRequest::taskPrompt)
                .build();

        Object out = agent.run("hello");
        assertEquals("Say: hello", out);
    }

    @Test
    void runReturnsRawStringWhenReturnTypeUnset() {
        LlmAgent agent = LlmAgent.builder()
                .name("Echo")
                .about("Echoes output.")
                .task("Hi {input}")
                .model(Model.of("test", "model"))
                .modelConfig(ModelConfiguration.temperature(0).maxTokenOutput(10))
                .llmExecutor(LlmRequest::taskPrompt)
                .build();

        Object out = agent.run("there");
        assertEquals("Hi there", out);
    }
}
