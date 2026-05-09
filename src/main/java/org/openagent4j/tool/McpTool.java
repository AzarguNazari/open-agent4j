package org.openagent4j.tool;

import java.util.Objects;

/**
 * Reference to a tool exposed by an MCP (Model Context Protocol) server.
 */
public record McpTool(String serverName, String toolName) {

    public McpTool {
        Objects.requireNonNull(serverName, "serverName");
        Objects.requireNonNull(toolName, "toolName");
    }

    public static McpTool of(String serverName, String toolName) {
        return new McpTool(serverName, toolName);
    }
}
