package org.openagent4j.provider;

import java.util.List;
import java.util.Locale;
import org.openagent4j.model.Model;

/**
 * Provider metadata needed to resolve credentials/base URL and instantiate models.
 */
public record ProviderDescriptor(
        String id, String defaultBaseUrl, List<String> apiKeyEnvFallbacks, List<String> baseUrlEnvFallbacks) {

    public ProviderDescriptor {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id");
        }
        id = id.trim().toLowerCase(Locale.ROOT);
        defaultBaseUrl = normalize(defaultBaseUrl);
        apiKeyEnvFallbacks = apiKeyEnvFallbacks == null ? List.of() : List.copyOf(apiKeyEnvFallbacks);
        baseUrlEnvFallbacks = baseUrlEnvFallbacks == null ? List.of() : List.copyOf(baseUrlEnvFallbacks);
    }

    public static ProviderDescriptor adHoc(String id) {
        return new ProviderDescriptor(id, null, List.of(), List.of());
    }

    public Model model(String modelName) {
        return Model.of(id, modelName);
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
