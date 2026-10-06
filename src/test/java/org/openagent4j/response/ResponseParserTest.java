package org.openagent4j.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ResponseParserTest {

    record Answer(String answer) {}

    @Test
    void stringTypeReturnsRawText() {
        assertEquals("plain", ResponseParser.forType(String.class).parse("plain"));
    }

    @Test
    void unsetTypeReturnsRawText() {
        assertEquals("plain", ResponseParser.<Object>forType(null).parse("plain"));
    }

    @Test
    void otherTypesRequireCustomParser() {
        IllegalArgumentException failure =
                assertThrows(IllegalArgumentException.class, () -> ResponseParser.forType(Answer.class));

        assertEquals(
                "No built-in parser for " + Answer.class.getName()
                        + ". Supply one with LlmAgent.builder().responseParser(...)",
                failure.getMessage());
    }
}
