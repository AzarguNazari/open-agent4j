package org.openagent4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openagent4j.config.OpenAgentProperties;
import org.openagent4j.execution.LlmRequest;
import org.openagent4j.execution.RetryPolicy;
import org.openagent4j.memory.LlmSession;
import org.openagent4j.memory.Memory;
import org.openagent4j.model.DeepSeek;
import org.openagent4j.model.Model;
import org.openagent4j.model.ModelConfiguration;
import org.openagent4j.model.OpenApi;
import org.openagent4j.model.ReasoningConfig;
import org.openagent4j.tool.AgentTool;
import org.openagent4j.tool.McpTool;
import org.openagent4j.tool.Tool;
import org.openagent4j.tool.ToolArguments;

class LlmAgentTest {

    record WeatherResponse(String city, String temperature, String nextDayPrediction) {}

    @Test
    void runBuildsRequestInterpolatesInputAndDeserializesReturnType() {
        String task = """
                {input}
                """;

        LlmAgent<WeatherResponse> weatherAgent = LlmAgent.builder(WeatherResponse.class)
                .name("Weather Agent")
                .about(
                        "Your are a weather agent and your task is to make sure to find the weather of a specific locaiton which is passed")
                .purpose("You are a weather assistant. Return concise weather JSON.")
                .task(task)
                .tools(
                        LlmAgent.tools(
                                Tool.of("lookup", "Resolve coordinates"), Tool.of("format", "Format report")))
                .mcps(
                        LlmAgent.mcps(
                                McpTool.of("weather-mcp", "current"),
                                McpTool.of("weather-mcp", "forecast")))
                .minConfidence(0.85)
                .memory(Memory.from("session-1", "ttl", "1h"))
                .model(Model.of("acme", "gpt-mock"))
                .modelConfig(ModelConfiguration.temperature(0).maxTokenOutput(500))
                .agentProperties(OpenAgentProperties.empty())
                .llmExecutor(req -> {
                    assertEquals("Weather Agent", req.agentName());
                    assertEquals("You are a weather assistant. Return concise weather JSON.", req.systemMessage());
                    assertEquals(
                            """
                            Oslo
                            """.trim(),
                            req.taskPrompt().trim());
                    assertEquals(2, req.tools().size());
                    assertEquals(2, req.mcpTools().size());
                    assertEquals(0.85, req.minConfidence());
                    assertEquals("session-1", req.memory().storeId());
                    assertEquals("acme", req.model().provider());
                    assertEquals(0.0, req.modelConfig().temperature());
                    assertEquals(500, req.modelConfig().maxTokenOutput());
                    assertEquals("acme", req.providerSettings().providerId());
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
        LlmAgent<String> agent = LlmAgent.builder(String.class)
                .name("Echo")
                .about("Echoes output.")
                .task("Say: {input}")
                .model(Model.of("test", "model"))
                .modelConfig(ModelConfiguration.temperature(0).maxTokenOutput(10))
                .agentProperties(OpenAgentProperties.empty())
                .llmExecutor(LlmRequest::taskPrompt)
                .build();

        Object out = agent.run("hello");
        assertEquals("Say: hello", out);
    }

    @Test
    void runReturnsRawStringWhenReturnTypeUnset() {
        LlmAgent<Object> agent = LlmAgent.builder()
                .name("Echo")
                .about("Echoes output.")
                .task("Hi {input}")
                .model(Model.of("test", "model"))
                .modelConfig(ModelConfiguration.temperature(0).maxTokenOutput(10))
                .agentProperties(OpenAgentProperties.empty())
                .llmExecutor(LlmRequest::taskPrompt)
                .build();

        Object out = agent.run("there");
        assertEquals("Hi there", out);
    }

    @Test
    void runWithoutInputUsesTaskAsPrompt() {
        LlmAgent<String> agent = LlmAgent.builder(String.class)
                .name("Math")
                .about("Helper")
                .task("What is the result of 2 + 2?")
                .model(Model.of("test", "mock"))
                .agentProperties(OpenAgentProperties.empty())
                .llmExecutor(req -> {
                    assertEquals("What is the result of 2 + 2?", req.taskPrompt().trim());
                    return "4";
                })
                .build();

        assertEquals("4", agent.run());
    }

    @Test
    void responseMethodSetsReturnType() {
        LlmAgent<String> agent = LlmAgent.builder()
                .name("A")
                .about("B")
                .task("ping")
                .response(String.class)
                .model(Model.of("t", "m"))
                .agentProperties(OpenAgentProperties.empty())
                .llmExecutor(r -> "ok")
                .build();

        assertEquals("ok", agent.run());
    }

    @Test
    void typedResponseIsExposedOnLlmRequest() {
        LlmAgent<WeatherResponse> agent = LlmAgent.builder()
                .name("Typed")
                .about("Expose configured return type to executor.")
                .task("weather")
                .response(WeatherResponse.class)
                .model(Model.of("test", "model"))
                .agentProperties(OpenAgentProperties.empty())
                .llmExecutor(req -> {
                    try {
                        Object responseType = req.getClass().getMethod("responseType").invoke(req);
                        assertEquals(WeatherResponse.class, responseType);
                    } catch (ReflectiveOperationException e) {
                        throw new AssertionError("LlmRequest should expose configured response type", e);
                    }
                    return """
                            {"city":"Oslo","temperature":"5C","nextDayPrediction":"rain"}
                            """;
                })
                .build();

        WeatherResponse out = agent.run();
        assertEquals("Oslo", out.city());
    }

    @Test
    void purposeIsIncludedInSystemMessage() {
        LlmAgent<String> agent = LlmAgent.builder(String.class)
                .name("N")
                .about("About line.")
                .purpose("Follow instructions carefully.")
                .task("x")
                .model(Model.of("p", "m"))
                .agentProperties(OpenAgentProperties.empty())
                .llmExecutor(r -> {
                    assertEquals("Follow instructions carefully.", r.systemMessage());
                    return "y";
                })
                .build();

        assertEquals("y", agent.run());
    }

    @Test
    void serviceObjectRegistersAgentTools() {
        LlmAgent<String> agent = LlmAgent.builder(String.class)
                .name("W")
                .about("weather")
                .task("run tools")
                .tools(new WeatherToolService())
                .model(Model.of("x", "y"))
                .agentProperties(OpenAgentProperties.empty())
                .llmExecutor(r -> {
                    assertEquals(1, r.tools().size());
                    assertEquals("currentWeather", r.tools().getFirst().name());
                    return "done";
                })
                .build();

        assertEquals("done", agent.run());
    }

    @Test
    void builtInModelsAndReasoningAppearOnRequest() {
        LlmAgent<String> agent = LlmAgent.builder(String.class)
                .name("WeatherAgent")
                .about("agent for weather information")
                .task("What is the weather of London today?")
                .model(DeepSeek.V3())
                .reasoningConfig(
                        ReasoningConfig.builder().includeThoughts(true).maxThinkingTokens(1000).build())
                .retryPolicy(RetryPolicy.exponentialBackoff(3))
                .session(LlmSession.newPersistentSession("s1"))
                .agentProperties(OpenAgentProperties.empty())
                .llmExecutor(req -> {
                    assertEquals("deepseek", req.model().provider());
                    assertEquals("deepseek-chat", req.model().modelName());
                    assertEquals(true, req.reasoningConfig().includeThoughts());
                    assertEquals(1000, req.reasoningConfig().maxThinkingTokens());
                    assertEquals(3, req.retryPolicy().maxAttempts());
                    assertTrue(req.session().persistent());
                    assertEquals("s1", req.session().sessionId());
                    return "ok";
                })
                .build();

        assertEquals("ok", agent.run());
    }

    @Test
    void providerSettingsResolveSystemProperties() {
        System.setProperty("openagent4j.openai.api-key", "secret-from-system");
        try {
            LlmAgent<String> agent = LlmAgent.builder(String.class)
                    .name("Open")
                    .about("AI")
                    .task("hi")
                    .model(OpenApi.gpt35Turbo())
                    .agentProperties(OpenAgentProperties.empty())
                    .llmExecutor(req -> {
                        assertEquals("secret-from-system", req.providerSettings().apiKey());
                        assertEquals("openai", req.providerSettings().providerId());
                        return "ok";
                    })
                    .build();

            assertEquals("ok", agent.run());
        } finally {
            System.clearProperty("openagent4j.openai.api-key");
        }
    }

    static final class WeatherToolService {
        @AgentTool(name = "currentWeather", description = "Get weather for city")
        public String current(ToolArguments args) {
            return args.getString("city");
        }
    }
}
