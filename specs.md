
```java


  String task = """
        {input}
  """;

  record WeatherResponse(String city, String temperature, String nextDayPrediction) {}
LlmSession session = LlmSession.newPersistentSession("user-123");

Tool.builder("get_weather")
    .action(weatherService::fetch)
    .preValidator(args -> {
        if (args.getString("city").isEmpty()) throw new InvalidArgsException("City is required");
    })
    .build();

LlmAgent<WeatherResponse> weatherAgent = LlmAgent.builder(WeatherResponse.class)
    .name("Weather Expert")
    // 1. Multi-Model Fallback: If OpenAI hits a rate limit, swap to DeepSeek automatically.
    .model(
        Model.of("gpt-4o")
             .withFallback(Model.of("deepseek-reasoner")) 
    )
    // 2. Specialized Reasoning: Support for DeepSeek R1 / OpenAI o1 'thinking' tokens.
    .reasoningConfig(ReasoningConfig.builder()
        .includeThoughts(true) // Allows you to capture the <thought> tags
        .maxThinkingTokens(1000)
        .build())
    
    // 3. Resilience: Don't let a bad tool call kill the agent.
    .retryPolicy(RetryPolicy.exponentialBackoff(3))
    
    // 4. State & Memory: Using the session to maintain context across different .run() calls.
    .session(session)
    
    // 5. Hooks for Observability (Crucial for debugging agents)
    .onStep(step -> log.info("Agent is currently: {}", step.action()))
    .onToolError((tool, error) -> log.error("Tool {} failed, attempting recovery...", tool))
    
    .build();

// Use a Future or Flux for streaming the agent's "train of thought"
CompletableFuture<WeatherResponse> result = weatherAgent.runAsync("What's it like in London?");
```


