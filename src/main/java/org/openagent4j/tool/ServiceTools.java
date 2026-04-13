package org.openagent4j.tool;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
        List<Tool> out = new ArrayList<>();
        for (Method method : instance.getClass().getMethods()) {
            AgentTool meta = method.getAnnotation(AgentTool.class);
            if (meta == null) {
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            String name = meta.name().isBlank() ? method.getName() : meta.name();
            String description = meta.description().isBlank() ? name : meta.description();
            int paramCount = method.getParameterCount();
            if (paramCount > 1) {
                throw new IllegalArgumentException(
                        "AgentTool method " + method + " must have 0 or 1 parameter (ToolArguments)");
            }
            if (paramCount == 1 && method.getParameterTypes()[0] != ToolArguments.class) {
                throw new IllegalArgumentException(
                        "AgentTool method " + method + " must use ToolArguments as its single parameter");
            }
            Tool tool = Tool.builder(name)
                    .description(description)
                    .action(args -> invoke(method, instance, args))
                    .build();
            out.add(tool);
        }
        if (out.isEmpty()) {
            throw new IllegalArgumentException("No @AgentTool methods on " + instance.getClass().getName());
        }
        return List.copyOf(out);
    }

    private static Object invoke(Method method, Object instance, ToolArguments args) {
        try {
            if (method.getParameterCount() == 0) {
                return method.invoke(instance);
            }
            return method.invoke(instance, args);
        } catch (ReflectiveOperationException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new IllegalStateException(cause);
        }
    }
}
