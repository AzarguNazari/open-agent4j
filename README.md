# open-agent4j

open-agent4j is a Java library for building agentic LLM interactions. It provides a simple API to manage agent workflows with built-in resilience, multi-model fallbacks, and reasoning support.

## Features

- Multi-model fallback (e.g., OpenAI to DeepSeek).
- Specialized reasoning (thinking tokens) support.
- Resilience with retry policies.
- State management via sessions.
- Hooks for step-by-step observability.

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.openagent4j</groupId>
    <artifactId>open-agent4j</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

```java
import org.openagent4j.*;
import org.openagent4j.config.OpenAgentProperties;
import org.openagent4j.model.Model;
import org.openagent4j.model.OpenApi;

LlmAgent<WeatherResponse> weatherAgent = LlmAgent.builder(WeatherResponse.class)
    .name("Weather Expert")
    .about("Answers weather questions with tools when needed")
    .purpose("Be concise. Use metric units unless asked otherwise.")
    .task("{input}")
    .model(
        Model.of("openai", "gpt-4o")
             .withFallback(org.openagent4j.model.DeepSeek.V3())
    )
    .retryPolicy(RetryPolicy.exponentialBackoff(3))
    .session(LlmSession.newPersistentSession("user-123"))
    .agentProperties(OpenAgentProperties.load())
    .llmExecutor(request -> {
        // Provide an implementation that reads request.providerSettings() (api key, base URL)
        throw new UnsupportedOperationException("Wire your HTTP client here");
    })
    .build();

CompletableFuture<WeatherResponse> result = weatherAgent.runAsync("What's it like in London?");
```

### Provider configuration

Add `src/main/resources/openagent4j.properties` (or rely on environment variables). Example keys:

- `openagent4j.openai.api-key` / `openagent4j.openai.base-url`
- `openagent4j.deepseek.api-key` / `openagent4j.deepseek.base-url`

Environment overrides such as `OPENAGENT4J_OPENAI_API_KEY`, `OPENAGENT4J_DEEPSEEK_BASE_URL`, and legacy names (`OPENAI_API_KEY`, `DEEPSEEK_API_KEY`, etc. — see `LlmProvider`) are also supported. Resolved values are exposed on `LlmRequest.providerSettings()` for your `LlmExecutor`.

### Adding providers and models

- **Built-in defaults**: add an enum constant to `org.openagent4j.provider.LlmProvider` (default base URL + optional legacy env vars), and add models to `org.openagent4j.provider.KnownModel`. Facades like `OpenApi` / `DeepSeek` can delegate to `KnownModel` for stable names.
- **Ad hoc providers**: use `Model.of("vendor-id", "model-name")` with `openagent4j.vendor-id.api-key` / `.base-url` (or `OPENAGENT4J_VENDOR_ID_*`). For vendors not in `LlmProvider`, you must configure a base URL; `OpenAiCompatibleLlmExecutor` will not guess one.

## Contributing

Contributions are welcome via Pull Requests.

## License

MIT License. See `LICENSE` for details.

Copyright (c) 2026 Hazar Gul Nazari
