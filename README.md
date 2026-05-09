# open-agent4j

open-agent4j is a Java library for building agentic LLM interactions. It provides a simple API to manage agent workflows with built-in resilience, multi-model fallbacks, and reasoning support.

> [!NOTE]
> open-agent4j is currently not available on Maven Central. You must clone the repository and install it locally to use it in your projects.

## Features

- Multi-model fallback (e.g., OpenAI to DeepSeek).
- Specialized reasoning (thinking tokens) support.
- Resilience with retry policies.
- State management via persistent memory sessions.
- Execution callbacks for step-by-step observability.
- First-class support for POJO/Record structured outputs.

## Installation

Since the library is not yet on Maven Central, you need to clone and install it to your local Maven repository:

```bash
git clone https://github.com/AzarguNazari/open-agent4j.git
cd open-agent4j
mvn clean install
```

Then, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.openagent4j</groupId>
    <artifactId>open-agent4j</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

The framework ships with an `OpenAiCompatibleLlmExecutor` which works out of the box with OpenAI, DeepSeek, and other compatible providers. Just make sure you have the respective API key set in your environment variables (e.g., `DEEPSEEK_API_KEY`).

### 1. Simple Structured Output Agent

This agent returns a strictly typed `Answer` record instead of a plain string.

```java
import org.openagent4j.*;
import org.openagent4j.memory.LlmSession;
import org.openagent4j.model.DeepSeek;
import org.openagent4j.execution.OpenAiCompatibleLlmExecutor;

public class App {
    record Answer(String answer) {}

    public static void main(String[] args) {
        LlmAgent<Answer> chatAgent = LlmAgent.<Answer>builder()
                .name("Math Helper")
                .purpose("You are a helpful assistant. Answer questions accurately and concisely.")
                .task("{input}")
                .model(DeepSeek.V4_Flash())
                .returnType(Answer.class)
                .llmExecutor(new OpenAiCompatibleLlmExecutor())
                .build();

        Answer response = chatAgent.run("What is the result of 2 + 2?");
        System.out.println(response.answer());
    }
}
```

### 2. Advanced: Memory, Fallbacks, and Observability

This example demonstrates how to use persistent memory sessions, add multi-model fallbacks, and track real-time agent execution steps.

```java
import org.openagent4j.*;
import org.openagent4j.memory.LlmSession;
import org.openagent4j.model.Model;
import org.openagent4j.model.OpenApi;
import org.openagent4j.execution.OpenAiCompatibleLlmExecutor;
import org.openagent4j.execution.RetryPolicy;

LlmAgent<String> supportAgent = LlmAgent.<String>builder()
    .name("Support Expert")
    .purpose("Answer support questions clearly.")
    .task("{input}")
    .model(
        OpenApi.gpt4o()
             .withFallback(org.openagent4j.model.DeepSeek.V4_Flash())
    )
    .retryPolicy(RetryPolicy.exponentialBackoff(3))
    .session(LlmSession.newPersistentSession("user-session-123"))
    .onStep(step -> System.out.println("[Agent Step] " + step.action()))
    .llmExecutor(new OpenAiCompatibleLlmExecutor())
    .build();

String result = supportAgent.run("How do I reset my password?");
System.out.println(result);
```

## Provider Configuration

You can configure providers via `src/main/resources/openagent4j.properties` or environment variables:

- `openagent4j.openai.api-key` / `openagent4j.openai.base-url`
- `openagent4j.deepseek.api-key` / `openagent4j.deepseek.base-url`

Environment overrides such as `OPENAGENT4J_OPENAI_API_KEY`, `OPENAGENT4J_DEEPSEEK_BASE_URL`, and legacy names (`OPENAI_API_KEY`, `DEEPSEEK_API_KEY`, etc. — see `LlmProvider`) are fully supported out of the box. 

## Contributing

Contributions are welcome via Pull Requests.

## License

MIT License. See `LICENSE` for details.

Copyright (c) 2026 Hazar Gul Nazari
