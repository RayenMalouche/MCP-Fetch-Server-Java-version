package com.mcp.RayenMalouche.java.server.Fetch.tools;

import com.mcp.RayenMalouche.java.server.Fetch.service.WebContentService;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

public class GetMarkdownTool extends BaseFetchTool {

    private final WebContentService webContentService;

    public GetMarkdownTool(WebContentService webContentService) {
        this.webContentService = webContentService;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification getToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(
                        "get_markdown",
                        "Converts web page content to well-formatted Markdown, preserving structural " +
                                "elements like tables and definition lists. Recommended as the default tool " +
                                "for web content extraction when a clean, readable text format is needed " +
                                "while maintaining document structure.",
                        createUrlInputSchema()
                ),
                (exchange, params) -> {
                    try {
                        String url = validateAndGetUrl(params);
                        String content = webContentService.getMarkdownContent(url);
                        return createSuccessResult(content);
                    } catch (IllegalArgumentException e) {
                        return createErrorResult("Invalid parameters: " + e.getMessage());
                    } catch (Exception e) {
                        return createErrorResult("Failed to convert content to markdown: " + e.getMessage());
                    }
                }
        );
    }
}