package org.openagent4j.model;

import org.openagent4j.provider.KnownModel;

/**
 * Built-in OpenAI-compatible model identifiers.
 */
public final class OpenApi {

    private OpenApi() {}

    public static Model gpt35Turbo() {
        return KnownModel.GPT_35_TURBO.asModel();
    }
}
