package org.openagent4j.execution;

import lombok.Builder;

@Builder(toBuilder = true)
public record AgentStep(String action) {}
