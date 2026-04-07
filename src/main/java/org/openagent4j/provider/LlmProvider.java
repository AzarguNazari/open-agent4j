package org.openagent4j.provider;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.openagent4j.model.Model;

/**
 * First-party provider identifiers, default API base URLs, and conventional environment-variable fallbacks for API keys
 * and base URLs (after {@code openagent4j.*} keys and {@code OPENAGENT4J_<PROVIDER>_*} are checked).
 *
 * <p>Add a new provider here when baking in defaults; arbitrary provider ids still work via {@link Model#of(String, String)}
 * together with {@code openagent4j.properties} or {@code OPENAGENT4J_*} env vars, but {@link org.openagent4j.execution.OpenAiCompatibleLlmExecutor}
 * requires an explicit base URL for providers not listed here.
 */
public enum LlmProvider {
    OPENAI(
            "openai",
            "https://api.openai.com/v1",
            List.of("OPENAI_API_KEY", "OPENAI_API_SECRET"),
            List.of("OPENAI_BASE_URL")),
    DEEPSEEK(
            "deepseek",
            "https://api.deepseek.com/v1",
            List.of("DEEPSEEK_API_KEY"),
            List.of("DEEPSEEK_BASE_URL"));

    private final String id;
    private final String defaultBaseUrl;
    private final List<String> apiKeyEnvFallbacks;
    private final List<String> baseUrlEnvFallbacks;

    LlmProvider(
            String id,
            String defaultBaseUrl,
            List<String> apiKeyEnvFallbacks,
            List<String> baseUrlEnvFallbacks) {
        this.id = id;
        this.defaultBaseUrl = defaultBaseUrl;
        this.apiKeyEnvFallbacks = List.copyOf(apiKeyEnvFallbacks);
        this.baseUrlEnvFallbacks = List.copyOf(baseUrlEnvFallbacks);
    }

    public String id() {
        return id;
    }

    /**
     * Default REST API prefix (no trailing slash) for OpenAI-compatible chat completions.
     */
    public String defaultBaseUrl() {
        return defaultBaseUrl;
    }

    public List<String> apiKeyEnvFallbacks() {
        return apiKeyEnvFallbacks;
    }

    public List<String> baseUrlEnvFallbacks() {
        return baseUrlEnvFallbacks;
    }

    public Model model(String modelName) {
        return Model.of(id, modelName);
    }

    public static Optional<LlmProvider> byId(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return Optional.empty();
        }
        String normalized = providerId.trim().toLowerCase(Locale.ROOT);
        for (LlmProvider value : values()) {
            if (value.id.equals(normalized)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
