package com.mcp.RayenMalouche.java.server.Fetch.tools;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;

public abstract class BaseFetchTool {

    protected static final String URL_PARAMETER = "url";

    /**
     * Creates the tool specification for this fetch tool
     */
    public abstract McpServerFeatures.SyncToolSpecification getToolSpecification();

    /**
     * Validates that the URL parameter is present and valid
     */
    protected String validateAndGetUrl(Map<String, Object> params) throws IllegalArgumentException {
        Object urlObj = params.get(URL_PARAMETER);
        if (urlObj == null) {
            throw new IllegalArgumentException("url parameter is required");
        }

        String url = urlObj.toString().trim();
        if (url.isEmpty()) {
            throw new IllegalArgumentException("url parameter cannot be empty");
        }

        // Basic URL validation
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("url must start with http:// or https://");
        }

        return url;
    }

    /**
     * Creates a successful tool result with text content
     */
    protected McpSchema.CallToolResult createSuccessResult(String content) {
        return new McpSchema.CallToolResult(
                List.of(new McpSchema.TextContent(content)),
                false
        );
    }

    /**
     * Creates an error tool result
     */
    protected McpSchema.CallToolResult createErrorResult(String error) {
        return new McpSchema.CallToolResult(
                List.of(new McpSchema.TextContent("Error: " + error)),
                true
        );
    }

    /**
     * Creates the standard URL input schema used by all fetch tools
     */
    protected String createUrlInputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "url": {
                      "type": "string",
                      "description": "URL of the target resource"
                    }
                  },
                  "required": ["url"]
                }
                """;
    }
}