package org.openagent4j.model;

public record ReasoningConfig(Boolean includeThoughts, Integer maxThinkingTokens) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Boolean includeThoughts;
        private Integer maxThinkingTokens;

        private Builder() {}

        public Builder includeThoughts(Boolean includeThoughts) {
            this.includeThoughts = includeThoughts;
            return this;
        }

        public Builder maxThinkingTokens(Integer maxThinkingTokens) {
            this.maxThinkingTokens = maxThinkingTokens;
            return this;
        }

        public ReasoningConfig build() {
            return new ReasoningConfig(includeThoughts, maxThinkingTokens);
        }
    }
}
