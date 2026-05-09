package org.openagent4j.tool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method as an invokable agent tool. The method should accept {@link ToolArguments} (or no parameters).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AgentTool {

    /**
     * Tool name; defaults to the Java method name.
     */
    String name() default "";

    /**
     * Human-readable description for the model.
     */
    String description() default "";
}
