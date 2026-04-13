package org.openagent4j.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record ReasoningConfig(Boolean includeThoughts, Integer maxThinkingTokens) {
}
