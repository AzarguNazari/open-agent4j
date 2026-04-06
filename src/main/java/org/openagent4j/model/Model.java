package org.openagent4j.model;

import java.util.Objects;

/**
 * Identifies a concrete model within a provider (for example {@code openai / gpt-4o}).
 */
public record Model(String provider, String modelName) {

    public Model {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(modelName, "modelName");
    }

    public static Model of(String provider, String modelName) {
        return new Model(provider, modelName);
    }
}
