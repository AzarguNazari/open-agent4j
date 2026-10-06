package org.openagent4j.execution;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extracts the assistant message text from an OpenAI-compatible chat completions JSON response.
 */
final class ChatCompletionResponseReader {

    private ChatCompletionResponseReader() {}

    static String readContent(String responseText) {
        Object root = new JsonCursor(responseText).readDocument();
        failOnApiError(root);
        Object choices = member(root, "choices");
        if (!(choices instanceof List<?> choiceList) || choiceList.isEmpty()) {
            throw new IllegalStateException("Unexpected response (no choices): " + responseText);
        }
        Object content = member(member(choiceList.getFirst(), "message"), "content");
        if (content == null) {
            throw new IllegalStateException("Unexpected response (no message content): " + responseText);
        }
        return content.toString();
    }

    private static void failOnApiError(Object root) {
        Object error = member(root, "error");
        if (error == null) {
            return;
        }
        Object message = member(error, "message");
        if (message != null) {
            throw new IllegalStateException("Chat API error: " + message);
        }
        throw new IllegalStateException("Chat API error: " + error);
    }

    private static Object member(Object node, String key) {
        if (node instanceof Map<?, ?> object) {
            return object.get(key);
        }
        return null;
    }

    /**
     * Minimal JSON reader: objects become maps, arrays lists, numbers {@link BigDecimal}, and literals their Java equivalent.
     */
    private static final class JsonCursor {

        private final String text;
        private int position;

        private JsonCursor(String text) {
            this.text = text;
        }

        private Object readDocument() {
            Object value = readValue();
            skipWhitespace();
            if (position != text.length()) {
                throw failure("unexpected trailing content");
            }
            return value;
        }

        private Object readValue() {
            skipWhitespace();
            if (position >= text.length()) {
                throw failure("unexpected end of input");
            }
            char next = text.charAt(position);
            return switch (next) {
                case '{' -> readObject();
                case '[' -> readArray();
                case '"' -> readString();
                case 't' -> readLiteral("true", Boolean.TRUE);
                case 'f' -> readLiteral("false", Boolean.FALSE);
                case 'n' -> readLiteral("null", null);
                default -> readNumber();
            };
        }

        private Map<String, Object> readObject() {
            Map<String, Object> members = new LinkedHashMap<>();
            position++;
            skipWhitespace();
            if (consumeIf('}')) {
                return members;
            }
            do {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                members.put(key, readValue());
                skipWhitespace();
            } while (consumeIf(','));
            expect('}');
            return members;
        }

        private List<Object> readArray() {
            List<Object> elements = new ArrayList<>();
            position++;
            skipWhitespace();
            if (consumeIf(']')) {
                return elements;
            }
            do {
                elements.add(readValue());
                skipWhitespace();
            } while (consumeIf(','));
            expect(']');
            return elements;
        }

        private String readString() {
            expect('"');
            StringBuilder value = new StringBuilder();
            while (position < text.length()) {
                char character = text.charAt(position++);
                if (character == '"') {
                    return value.toString();
                }
                if (character == '\\') {
                    value.append(readEscape());
                    continue;
                }
                value.append(character);
            }
            throw failure("unterminated string");
        }

        private char readEscape() {
            if (position >= text.length()) {
                throw failure("unterminated escape");
            }
            char escape = text.charAt(position++);
            return switch (escape) {
                case 'n' -> '\n';
                case 'r' -> '\r';
                case 't' -> '\t';
                case 'b' -> '\b';
                case 'f' -> '\f';
                case 'u' -> readUnicodeEscape();
                case '"', '\\', '/' -> escape;
                default -> throw failure("invalid escape \\" + escape);
            };
        }

        private char readUnicodeEscape() {
            if (position + 4 > text.length()) {
                throw failure("invalid unicode escape");
            }
            try {
                char decoded = (char) Integer.parseInt(text.substring(position, position + 4), 16);
                position += 4;
                return decoded;
            } catch (NumberFormatException exception) {
                throw failure("invalid unicode escape");
            }
        }

        private Object readLiteral(String literal, Object value) {
            if (!text.startsWith(literal, position)) {
                throw failure("unexpected token");
            }
            position += literal.length();
            return value;
        }

        private BigDecimal readNumber() {
            int start = position;
            while (position < text.length() && "+-.eE0123456789".indexOf(text.charAt(position)) >= 0) {
                position++;
            }
            try {
                return new BigDecimal(text.substring(start, position));
            } catch (NumberFormatException exception) {
                throw failure("unexpected token");
            }
        }

        private void skipWhitespace() {
            while (position < text.length() && Character.isWhitespace(text.charAt(position))) {
                position++;
            }
        }

        private boolean consumeIf(char expected) {
            if (position < text.length() && text.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }

        private void expect(char expected) {
            if (!consumeIf(expected)) {
                throw failure("expected '" + expected + "'");
            }
        }

        private IllegalStateException failure(String reason) {
            return new IllegalStateException("Chat request failed: invalid JSON response (" + reason + " at " + position + ")");
        }
    }
}
