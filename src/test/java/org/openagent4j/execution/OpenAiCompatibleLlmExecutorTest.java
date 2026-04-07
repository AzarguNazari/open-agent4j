package org.openagent4j.execution;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.openagent4j.config.ProviderSettings;
import org.openagent4j.model.Model;

class OpenAiCompatibleLlmExecutorTest {

    @Test
    void throwsWhenBaseUrlMissingForUnknownProvider() {
        LlmRequest request = new LlmRequest(
                "n",
                "a",
                null,
                "task",
                Model.of("unknown-vendor", "some-model"),
                null,
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                new ProviderSettings("unknown-vendor", "sk-test", null));

        assertThrows(IllegalStateException.class, () -> new OpenAiCompatibleLlmExecutor().complete(request));
    }
}
