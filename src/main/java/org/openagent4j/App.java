package org.openagent4j;

import org.openagent4j.execution.OpenAiCompatibleLlmExecutor;
import org.openagent4j.memory.LlmSession;
import org.openagent4j.model.DeepSeek;

/**
 * Minimal runnable example: simple chat against DeepSeek using {@code DEEPSEEK_API_KEY} from the environment.
 */
public class App {

    public static void main(String[] args) {
        LlmSession session = LlmSession.newPersistentSession("demo-user");

        record Answer(String answer) {}
        LlmAgent<Answer> chatAgent = LlmAgent.builder()
                .name("SimpleChat")
                .about("A simple chat agent")
                .purpose(
                        "You are a helpful assistant. Your task is to answer questions honestly and accurately. Be concise.")
                .model(DeepSeek.V3())
                .onStep(step -> System.out.println("[step] " + step.action()))
                .task("What is the result of 2 + 2?")
                .session(session)
                .response(Answer.class)
                .llmExecutor(new OpenAiCompatibleLlmExecutor())
                .build();

        Answer response = chatAgent.run();
        System.out.println(response);
    }
}
