package org.openagent4j.tool;

import java.util.Map;

public record ToolArguments(Map<String, Object> arguments) {
    public ToolArguments {
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }

    public String getString(String key) {
        Object val = arguments.get(key);
        return val == null ? "" : String.valueOf(val);
    }
}
