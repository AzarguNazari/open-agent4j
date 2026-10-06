package org.openagent4j.tool;

import java.util.Map;

public record ToolArguments(Map<String, Object> arguments) {

    public ToolArguments {
        if (arguments == null) {
            arguments = Map.of();
        }
        arguments = Map.copyOf(arguments);
    }

    public String getString(String key) {
        Object value = arguments.get(key);
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
