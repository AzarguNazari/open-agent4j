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

    private final ProviderDescriptor descriptor;

    LlmProvider(
            String id,
            String defaultBaseUrl,
            List<String> apiKeyEnvFallbacks,
            List<String> baseUrlEnvFallbacks) {
        this.descriptor = new ProviderDescriptor(id, defaultBaseUrl, apiKeyEnvFallbacks, baseUrlEnvFallbacks);
    }

    public String id() {
        return descriptor.id();
    }

    /**
     * Default REST API prefix (no trailing slash) for OpenAI-compatible chat completions.
     */
    public String defaultBaseUrl() {
        return descriptor.defaultBaseUrl();
    }

    public List<String> apiKeyEnvFallbacks() {
        return descriptor.apiKeyEnvFallbacks();
    }

    public List<String> baseUrlEnvFallbacks() {
        return descriptor.baseUrlEnvFallbacks();
    }

    public Model model(String modelName) {
        return descriptor.model(modelName);
    }

    public ProviderDescriptor descriptor() {
        return descriptor;
    }

    public static Optional<LlmProvider> byId(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return Optional.empty();
        }
        String normalized = providerId.trim().toLowerCase(Locale.ROOT);
        for (LlmProvider value : values()) {
            if (value.id().equals(normalized)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
