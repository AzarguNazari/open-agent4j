package org.openagent4j.execution;

/**
 * Serializes an {@link LlmRequest} into an OpenAI-compatible chat completions JSON body.
 */
final class ChatCompletionRequestWriter {

    private ChatCompletionRequestWriter() {}

    static String write(LlmRequest request) {
        String systemMessage = request.systemMessage();
        StringBuilder body = new StringBuilder();
        body.append("{\"model\":").append(quote(request.model().modelName()));
        body.append(",\"messages\":[");
        if (request.expectsStructuredResponse()) {
            systemMessage = systemMessage + StructuredOutputInstruction.describe(request.responseType());
        }
        appendMessage(body, "system", systemMessage);
        body.append(',');
        appendMessage(body, "user", request.taskPrompt());
        body.append(']');
        if (request.expectsStructuredResponse()) {
            body.append(",\"response_format\":{\"type\":\"json_object\"}");
        }
        body.append('}');
        return body.toString();
    }

    private static void appendMessage(StringBuilder body, String role, String content) {
        body.append("{\"role\":").append(quote(role));
        body.append(",\"content\":").append(quote(content));
        body.append('}');
    }

    private static String quote(String text) {
        StringBuilder quoted = new StringBuilder(text.length() + 2);
        quoted.append('"');
        for (int index = 0; index < text.length(); index++) {
            appendEscaped(quoted, text.charAt(index));
        }
        return quoted.append('"').toString();
    }

    private static void appendEscaped(StringBuilder target, char character) {
        switch (character) {
            case '"' -> target.append("\\\"");
            case '\\' -> target.append("\\\\");
            case '\n' -> target.append("\\n");
            case '\r' -> target.append("\\r");
            case '\t' -> target.append("\\t");
            case '\b' -> target.append("\\b");
            case '\f' -> target.append("\\f");
            default -> appendPlain(target, character);
        }
    }

    private static void appendPlain(StringBuilder target, char character) {
        if (character < ' ') {
            target.append(String.format("\\u%04x", (int) character));
            return;
        }
        target.append(character);
    }
}
