package org.openagent4j.memory;

import lombok.Builder;

@Builder(toBuilder = true)
public record LlmSession(String sessionId, boolean persistent) {
    public static LlmSession newPersistentSession(String sessionId) {
        return new LlmSession(sessionId, true);
    }
}
