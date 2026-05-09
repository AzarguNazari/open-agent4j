package org.openagent4j.config;

import java.util.Objects;

/**
 * Credentials and endpoint overrides for a logical provider id (matches {@link org.openagent4j.model.Model#provider()}}).
 */
public record ProviderSettings(String providerId, String apiKey, String baseUrl) {

    public ProviderSettings {
        Objects.requireNonNull(providerId, "providerId");
    }

    public static ProviderSettings unresolved(String providerId) {
        return new ProviderSettings(providerId, null, null);
    }
}
