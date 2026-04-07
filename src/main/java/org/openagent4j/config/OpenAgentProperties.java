package org.openagent4j.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import org.openagent4j.model.Model;
import org.openagent4j.provider.LlmProvider;

/**
 * Resolves provider settings from {@code openagent4j.properties}, JVM system properties, and environment variables.
 *
 * <p>Precedence (highest first): system property, environment variable, classpath {@code openagent4j.properties},
 * then provider-specific legacy env vars defined on {@link LlmProvider}.
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
        String pid = providerId.trim();
        String keyApi = "openagent4j." + pid + ".api-key";
        String keyBase = "openagent4j." + pid + ".base-url";
        Optional<LlmProvider> known = LlmProvider.byId(pid);
        String api = firstNonBlank(
                System.getProperty(keyApi),
                env("OPENAGENT4J_" + envToken(pid) + "_API_KEY"),
                fileProps.getProperty(keyApi),
                firstEnv(known.map(LlmProvider::apiKeyEnvFallbacks).orElse(List.of())));
        String base = firstNonBlank(
                System.getProperty(keyBase),
                env("OPENAGENT4J_" + envToken(pid) + "_BASE_URL"),
                fileProps.getProperty(keyBase),
                firstEnv(known.map(LlmProvider::baseUrlEnvFallbacks).orElse(List.of())));
        return new ProviderSettings(pid, api, base);
    }

    private static String env(String name) {
        if (name == null) {
            return null;
        }
        try {
            return Optional.ofNullable(System.getenv(name)).map(String::trim).filter(s -> !s.isEmpty()).orElse(null);
        } catch (SecurityException e) {
            return null;
        }
    }

    private static String envToken(String providerId) {
        return providerId.toUpperCase(Locale.ROOT).replace('-', '_');
    }

    private static String firstEnv(List<String> names) {
        for (String n : names) {
            String v = env(n);
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }
}
