package org.openagent4j.tool;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reflectively exposes {@link AgentTool}-annotated methods on a service instance as {@link Tool} definitions.
 */
public final class ServiceTools {

    private ServiceTools() {}

    public static List<Tool> fromObject(Object instance) {
        Objects.requireNonNull(instance, "instance");
        List<Tool> tools = new ArrayList<>();
        for (Method method : instance.getClass().getMethods()) {
            AgentTool metadata = method.getAnnotation(AgentTool.class);
            if (metadata == null) {
                continue;
            }
            validateSignature(method);
            tools.add(toTool(method, metadata, instance));
        }
        if (tools.isEmpty()) {
            throw new IllegalArgumentException("No @AgentTool methods on " + instance.getClass().getName());
        }
        return List.copyOf(tools);
    }

    private static void validateSignature(Method method) {
        int parameterCount = method.getParameterCount();
        if (parameterCount > 1) {
            throw new IllegalArgumentException(
                    "AgentTool method " + method + " must have 0 or 1 parameter (ToolArguments)");
        }
        if (parameterCount == 1 && method.getParameterTypes()[0] != ToolArguments.class) {
            throw new IllegalArgumentException(
                    "AgentTool method " + method + " must use ToolArguments as its single parameter");
        }
    }

    private static Tool toTool(Method method, AgentTool metadata, Object instance) {
        String name = valueOrDefault(metadata.name(), method.getName());
        String description = valueOrDefault(metadata.description(), name);
        return Tool.builder(name)
                .description(description)
                .action(arguments -> invoke(method, instance, arguments))
                .build();
    }

    private static String valueOrDefault(String value, String defaultValue) {
        if (value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private static Object invoke(Method method, Object instance, ToolArguments arguments) {
        try {
            if (method.getParameterCount() == 0) {
                return method.invoke(instance);
            }
            return method.invoke(instance, arguments);
        } catch (ReflectiveOperationException exception) {
            throw unwrap(exception);
        }
    }

    private static RuntimeException unwrap(ReflectiveOperationException exception) {
        Throwable cause = exception.getCause();
        if (cause == null) {
            return new IllegalStateException(exception);
        }
        if (cause instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        return new IllegalStateException(cause);
    }
}
