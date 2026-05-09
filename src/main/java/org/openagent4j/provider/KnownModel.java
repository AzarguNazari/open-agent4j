package org.openagent4j.provider;

import org.openagent4j.model.Model;

/**
 * Curated catalog of model ids. Add entries here as you expand first-party support.
 */
public enum KnownModel {
    GPT_35_TURBO("openai.gpt-3.5-turbo", LlmProvider.OPENAI, "gpt-3.5-turbo"),
    DEEPSEEK_CHAT("deepseek.deepseek-chat", LlmProvider.DEEPSEEK, "deepseek-chat"),
    DEEPSEEK_V4_FLASH("deepseek.deepseek-v4-flash", LlmProvider.DEEPSEEK, "deepseek-v4-flash"),
    DEEPSEEK_V4_PRO("deepseek.deepseek-v4-pro", LlmProvider.DEEPSEEK, "deepseek-v4-pro");

    private final String alias;
    private final LlmProvider provider;
    private final String modelName;

    KnownModel(String alias, LlmProvider provider, String modelName) {
        this.alias = alias;
        this.provider = provider;
        this.modelName = modelName;
    }

    public String alias() {
        return alias;
    }

    public LlmProvider provider() {
        return provider;
    }

    public String modelName() {
        return modelName;
    }

    public Model asModel() {
        return provider.model(modelName);
    }
}
