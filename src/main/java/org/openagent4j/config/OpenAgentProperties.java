package org.openagent4j.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import org.openagent4j.model.Model;
import org.openagent4j.provider.ProviderDescriptor;
import org.openagent4j.provider.ProviderRegistry;

/**
 * Resolves provider settings from {@code openagent4j.properties}, JVM system properties, and environment variables.
 *
 * <p>Precedence (highest first): system property, environment variable, classpath {@code openagent4j.properties},
 * then provider-specific legacy env vars defined on built-in providers.
 */
public final class OpenAgentProperties {

    private static final String RESOURCE = "openagent4j.properties";

    private final Properties fileProps;

    private OpenAgentProperties(Properties fileProps) {
        this.fileProps = Objects.requireNonNull(fileProps, "fileProps");
    }

    public static OpenAgentProperties load() {
        Properties merged = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE)) {
            if (in != null) {
                merged.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + RESOURCE, e);
        }
        return new OpenAgentProperties(merged);
    }

    public static OpenAgentProperties empty() {
        return new OpenAgentProperties(new Properties());
    }

    public ProviderSettings resolve(Model model) {
        if (model == null || model.provider() == null || model.provider().isBlank()) {
            return ProviderSettings.unresolved("");
        }
        return resolve(model.provider());
    }

    public ProviderSettings resolve(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return ProviderSettings.unresolved("");
        }
        String normalizedId = providerId.trim();
        String environmentToken = ProviderSettings.environmentToken(normalizedId);
        String apiKeyProperty = "openagent4j." + normalizedId + ".api-key";
        String baseUrlProperty = "openagent4j." + normalizedId + ".base-url";
        Optional<ProviderDescriptor> descriptor = ProviderRegistry.defaults().find(normalizedId);
        String apiKey = firstNonBlank(
                System.getProperty(apiKeyProperty),
                environment("OPENAGENT4J_" + environmentToken + "_API_KEY"),
                fileProps.getProperty(apiKeyProperty),
                firstEnvironment(descriptor.map(ProviderDescriptor::apiKeyEnvFallbacks).orElse(List.of())));
        String baseUrl = firstNonBlank(
                System.getProperty(baseUrlProperty),
                environment("OPENAGENT4J_" + environmentToken + "_BASE_URL"),
                fileProps.getProperty(baseUrlProperty),
                firstEnvironment(descriptor.map(ProviderDescriptor::baseUrlEnvFallbacks).orElse(List.of())));
        return new ProviderSettings(normalizedId, apiKey, baseUrl);
    }

    private static String environment(String name) {
        try {
            return firstNonBlank(System.getenv(name));
        } catch (SecurityException exception) {
            return null;
        }
    }

    private static String firstEnvironment(List<String> names) {
        for (String name : names) {
            String value = environment(name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
