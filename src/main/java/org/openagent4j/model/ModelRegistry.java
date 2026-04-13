package org.openagent4j.model;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.openagent4j.provider.KnownModel;

/**
 * Immutable alias registry for model ids.
 */
public final class ModelRegistry {

    private static final ModelRegistry DEFAULTS = builder().register(KnownModel.values()).build();

    private final Map<String, Model> aliases;

    private ModelRegistry(Map<String, Model> aliases) {
        this.aliases = Map.copyOf(aliases);
    }

    public static ModelRegistry defaults() {
        return DEFAULTS;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(aliases);
    }

    public Set<String> aliases() {
        return aliases.keySet();
    }

    public Optional<Model> find(String alias) {
        String normalized = normalizeAlias(alias);
        if (normalized == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(aliases.get(normalized));
    }

    public Model require(String alias) {
        return find(alias).orElseThrow(() -> new IllegalArgumentException("Unknown model alias: " + alias));
    }

    private static String normalizeAlias(String alias) {
        if (alias == null || alias.isBlank()) {
            return null;
        }
        return alias.trim().toLowerCase(Locale.ROOT);
    }

    public static final class Builder {
        private final Map<String, Model> aliases = new LinkedHashMap<>();

        private Builder() {}

        private Builder(Map<String, Model> aliases) {
            this.aliases.putAll(aliases);
        }

        public Builder register(String alias, Model model) {
            String normalized = normalizeAlias(alias);
            if (normalized == null) {
                throw new IllegalArgumentException("alias");
            }
            aliases.put(normalized, Objects.requireNonNull(model, "model"));
            return this;
        }

        public Builder register(KnownModel knownModel) {
            Objects.requireNonNull(knownModel, "knownModel");
            return register(knownModel.alias(), knownModel.asModel());
        }

        public Builder register(KnownModel... knownModels) {
            if (knownModels == null) {
                return this;
            }
            for (KnownModel knownModel : knownModels) {
                register(knownModel);
            }
            return this;
        }

        public ModelRegistry build() {
            return new ModelRegistry(aliases);
        }
    }
}
