package org.openagent4j.model;

import org.openagent4j.provider.KnownModel;

/**
 * Built-in DeepSeek model identifiers.
 */
public final class DeepSeek {

    private DeepSeek() {}

    public static Model V3() {
        return KnownModel.DEEPSEEK_CHAT.asModel();
    }
}
