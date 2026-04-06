package org.openagent4j.memory;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Agent memory configuration (backing store id and optional attributes).
 */
public record Memory(String storeId, Map<String, Object> attributes) {

    public Memory {
        Objects.requireNonNull(storeId, "storeId");
        attributes = Map.copyOf(attributes);
    }

    public static Memory from(String storeId) {
        return new Memory(storeId, Map.of());
    }

    public static Memory from(String storeId, Map<String, Object> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return new Memory(storeId, Map.of());
        }
        return new Memory(storeId, attributes);
    }

    public static Memory from(String storeId, String key, Object value) {
        Objects.requireNonNull(key, "key");
        return new Memory(storeId, Collections.singletonMap(key, value));
    }
}
