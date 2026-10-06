package org.openagent4j;

import org.openagent4j.execution.OpenAiCompatibleLlmExecutor;
import org.openagent4j.memory.LlmSession;
import org.openagent4j.model.DeepSeek;

/**
 * Minimal runnable example: simple chat against DeepSeek using
 * {@code DEEPSEEK_API_KEY} from the environment.
 */
public class App {

    public static void main(String[] args) {
        LlmSession session = LlmSession.newPersistentSession("demo-user");

        LlmAgent<String> chatAgent = LlmAgent.<String>builder()
                .name("SimpleChat")
                .about("Your name is HazChat. You are here to help you in math.")
                .purpose(
                        "Your name is HazChat. You are here to help you in math. You are a helpful assistant. Your task is to answer questions honestly and accurately. Be concise.")
                .model(DeepSeek.V4_Flash())
                .onStep(step -> System.out.println("[step] " + step.action()))
                .task("What's your name?")
                .session(session)
                .returnType(String.class)
                .llmExecutor(new OpenAiCompatibleLlmExecutor())
                .build();

        String response = chatAgent.run();
        System.out.println("Response is " + response);
    }
}
