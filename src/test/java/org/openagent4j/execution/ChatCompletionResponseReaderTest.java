package org.openagent4j.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ChatCompletionResponseReaderTest {

    @Test
    void readsContentWithEscapesAndIgnoresOtherFields() {
        String response = """
                {"id":"x","usage":{"total":12,"ok":true,"none":null},
                 "choices":[{"index":0,"message":{"role":"assistant","content":"line\\n\\"quoted\\" \\u00e9"}}]}
                """;

        assertEquals("line\n\"quoted\" \u00e9", ChatCompletionResponseReader.readContent(response));
    }

    @Test
    void reportsApiErrorMessage() {
        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> ChatCompletionResponseReader.readContent("{\"error\":{\"message\":\"bad key\"}}"));

        assertEquals("Chat API error: bad key", failure.getMessage());
    }

    @Test
    void failsOnMissingChoices() {
        assertThrows(IllegalStateException.class, () -> ChatCompletionResponseReader.readContent("{}"));
    }

    @Test
    void failsOnInvalidJson() {
        assertThrows(IllegalStateException.class, () -> ChatCompletionResponseReader.readContent("not json"));
    }
}
