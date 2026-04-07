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

LlmAgent<WeatherResponse> weatherAgent = LlmAgent.builder(WeatherResponse.class)
    .name("Weather Expert")
    .model(
        Model.of("gpt-4o")
             .withFallback(Model.of("deepseek-reasoner"))
    )
    .retryPolicy(RetryPolicy.exponentialBackoff(3))
    .session(LlmSession.newPersistentSession("user-123"))
    .build();

CompletableFuture<WeatherResponse> result = weatherAgent.runAsync("What's it like in London?");
```

## Contributing

Contributions are welcome via Pull Requests.

## License

MIT License. See `LICENSE` for details.

Copyright (c) 2026 Hazar Gul Nazari
