
```java


  String task = """
        {input}
  """;

  record WeatherResponse(String city, String temperature, String nextDayPrediction) {}

  LlmAgent weatherAgent = LlmAgent.builder()
               .name("Weather Agent")
               .about("Your are a weather agent and your task is to make sure to find the weather of a specific locaiton which is passed")
               .task(task)
               .returnType(WeatherResponse)
               .internalTool(Tool.of(...), Tool.of(...))
               .mcp(McpTool.of(...), McpTool.of(...))
               .minConfidence(...)
               .memory(Memory.from(....))
               .model(Model.of(....))
               .modelConfig(ModelConfiguration.temperature(0).maxTokenOutput(500))
               .build();

weatherAgent.run(input);
```


