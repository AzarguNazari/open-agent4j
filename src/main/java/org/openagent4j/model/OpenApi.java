package org.openagent4j.model;

import org.openagent4j.provider.KnownModel;
import org.openagent4j.provider.LlmProvider;

/**
 * Built-in OpenAI-compatible model identifiers.
 */
public final class OpenApi {

    private OpenApi() {}

    /**
     * Resolve any OpenAI-hosted model id.
     */
    public static Model of(String modelName) {
        return LlmProvider.OPENAI.model(modelName);
    }

    public static Model gpt35Turbo() {
        return of(KnownModel.GPT_35_TURBO.modelName());
    }
}
