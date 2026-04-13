```java
    LlmSession session = LlmSession.newPersistentSession("user-123");
    LlmAgent chatAgent = LlmAgent.builder()
            .name("SimpleChat")
            .about("A simple chat agent")
            .purpose("You are a helpful assistant. Your task is to answer questions honestly and accurately.")
            .model(OpenApi.gpt35Turbo())
            .onStep(step -> log.info("Agent is currently: "))
            .task("What is the result of 2 + 2?")
            .session(session)
            .response(String.class)
            .build();
    String response = chatAgent.run();
```


```java
    record WeatherResponse(String city, String temperature) {}
    LlmAgent weatherAggent = LlmAgent.builder()
            .name("WeatherAgent")
            .about("agent for weather information")
            .purpose("You are a helpful assistant. Your task is to answer questions honestly and accurately.")
            .model(DeepSeek.V3())
            .tools(new WeatherService())
            .onStep(step -> log.info("Agent is currently: "))
            .onToolError((tool, error) -> log.error("Tool {} failed, attempting recovery...", tool))
            .task("What is the weather of London today?")
            .retryPolicy(RetryPolicy.exponentialBackoff(3))
            .reasoningConfig(ReasoningConfig.builder()
                    .includeThoughts(true) // Allows you to capture the <thought> tags
                    .maxThinkingTokens(1000)
                    .build())
            .response(WeatherResponse.class)
            .build();
    WeatherResponse response = weatherAggent.run();
```