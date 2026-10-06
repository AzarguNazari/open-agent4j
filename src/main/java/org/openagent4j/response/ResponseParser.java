package org.openagent4j.response;

/**
 * Converts the raw text returned by an {@link org.openagent4j.execution.LlmExecutor} into the agent's return type.
 * Implement this interface to support any output format (JSON, XML, ...) with the library of your choice.
 */
@FunctionalInterface
public interface ResponseParser<T> {

    T parse(String rawText);

    @SuppressWarnings("unchecked")
    static <T> ResponseParser<T> forType(Class<T> returnType) {
        if (returnType == null || returnType == String.class) {
            return rawText -> (T) rawText;
        }
        throw new IllegalArgumentException("No built-in parser for " + returnType.getName()
                + ". Supply one with LlmAgent.builder().responseParser(...)");
    }
}
