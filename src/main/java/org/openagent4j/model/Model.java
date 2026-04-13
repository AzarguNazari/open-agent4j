package org.openagent4j.model;

import lombok.Builder;
import java.util.Objects;

/**
 * Identifies a concrete model within a provider (for example {@code openai / gpt-4o}).
 */
@Builder(toBuilder = true)
public record Model(String provider, String modelName, Model fallback) {

    public Model {
        Objects.requireNonNull(modelName, "modelName");
    }

    public static Model of(String provider, String modelName) {
        return new Model(provider, modelName, null);
    }

    public static Model of(String modelName) {
        return new Model("default", modelName, null);
    }

    public Model withFallback(Model fallback) {
        return this.toBuilder().fallback(fallback).build();
    }
}
