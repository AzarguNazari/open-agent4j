package org.openagent4j.model;

import org.openagent4j.provider.KnownModel;
import org.openagent4j.provider.LlmProvider;

/**
 * Built-in DeepSeek model identifiers.
 */
public final class DeepSeek {

    private DeepSeek() {}

    /**
     * Resolve any DeepSeek-hosted model id.
     */
    public static Model of(String modelName) {
        return LlmProvider.DEEPSEEK.model(modelName);
    }

    public static Model V3() {
        return of(KnownModel.DEEPSEEK_CHAT.modelName());
    }
}
