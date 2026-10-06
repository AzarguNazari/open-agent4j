package org.openagent4j.execution;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.StringJoiner;

/**
 * Builds the prompt suffix that tells a model which JSON shape to return for a typed response.
 */
public final class StructuredOutputInstruction {

    private StructuredOutputInstruction() {}

    public static String describe(Class<?> responseType) {
        return "\nReturn ONLY valid JSON that can be deserialized to "
                + responseType.getName()
                + ". Expected shape: "
                + describeShape(responseType)
                + ".";
    }

    private static String describeShape(Class<?> responseType) {
        StringJoiner fields = new StringJoiner(", ", "{", "}");
        if (responseType.isRecord()) {
            for (RecordComponent component : responseType.getRecordComponents()) {
                fields.add(describeField(component.getName(), component.getType()));
            }
            return fields.toString();
        }
        for (Field field : responseType.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }
            fields.add(describeField(field.getName(), field.getType()));
        }
        return fields.toString();
    }

    private static String describeField(String name, Class<?> type) {
        return "\"" + name + "\": \"" + typeLabel(type) + "\"";
    }

    private static String typeLabel(Class<?> type) {
        if (type.isArray()) {
            return "array<" + typeLabel(type.getComponentType()) + ">";
        }
        if (type == String.class || type == char.class || type == Character.class) {
            return "string";
        }
        if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        }
        if (type.isPrimitive() || Number.class.isAssignableFrom(type)) {
            return "number";
        }
        return "object";
    }
}
