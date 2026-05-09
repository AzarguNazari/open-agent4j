package org.openagent4j.memory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Builder;

@Builder(toBuilder = true)
public record LlmSession(String sessionId, boolean persistent) {

    private static final ConcurrentMap<String, ConcurrentMap<String, Object>> SESSION_STATE = new ConcurrentHashMap<>();

    public LlmSession {
        Objects.requireNonNull(sessionId, "sessionId");
    }

    public static LlmSession newPersistentSession(String sessionId) {
        return new LlmSession(sessionId, true);
    }

    public Object state(String key) {
        Objects.requireNonNull(key, "key");
        return stateMap().get(key);
    }

    public Object putState(String key, Object value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        return stateMap().put(key, value);
    }

    public Object removeState(String key) {
        Objects.requireNonNull(key, "key");
        return stateMap().remove(key);
    }

    public void clearState() {
        stateMap().clear();
    }

    public Map<String, Object> snapshotState() {
        return Map.copyOf(stateMap());
    }

    private ConcurrentMap<String, Object> stateMap() {
        return SESSION_STATE.computeIfAbsent(sessionId, ignored -> new ConcurrentHashMap<>());
    }
}
