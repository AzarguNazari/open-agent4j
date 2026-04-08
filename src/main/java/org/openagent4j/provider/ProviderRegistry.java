package org.openagent4j.provider;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable provider catalog with a default built-in registry derived from {@link LlmProvider}.
 */
public final class ProviderRegistry {

    private static final ProviderRegistry DEFAULTS = builder().register(LlmProvider.values()).build();

    private final Map<String, ProviderDescriptor> descriptors;

    private ProviderRegistry(Map<String, ProviderDescriptor> descriptors) {
        this.descriptors = Map.copyOf(descriptors);
    }

    public static ProviderRegistry defaults() {
        return DEFAULTS;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(descriptors);
    }

    public Collection<ProviderDescriptor> all() {
        return descriptors.values();
    }

    public Optional<ProviderDescriptor> find(String providerId) {
        String normalized = normalize(providerId);
        if (normalized == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(descriptors.get(normalized));
    }

    public ProviderDescriptor resolve(String providerId) {
        String normalized = normalize(providerId);
        if (normalized == null) {
            throw new IllegalArgumentException("providerId");
        }
        return descriptors.getOrDefault(normalized, ProviderDescriptor.adHoc(normalized));
    }

    private static String normalize(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return null;
        }
        return providerId.trim().toLowerCase(Locale.ROOT);
    }

    public static final class Builder {
        private final Map<String, ProviderDescriptor> descriptors = new LinkedHashMap<>();

        private Builder() {}

        private Builder(Map<String, ProviderDescriptor> descriptors) {
            this.descriptors.putAll(descriptors);
        }

        public Builder register(LlmProvider provider) {
            Objects.requireNonNull(provider, "provider");
            return register(provider.descriptor());
        }

        public Builder register(LlmProvider... providers) {
            if (providers == null) {
                return this;
            }
            for (LlmProvider provider : providers) {
                register(provider);
            }
            return this;
        }

        public Builder register(ProviderDescriptor descriptor) {
            Objects.requireNonNull(descriptor, "descriptor");
            descriptors.put(descriptor.id(), descriptor);
            return this;
        }

        public ProviderRegistry build() {
            return new ProviderRegistry(descriptors);
        }
    }
}
