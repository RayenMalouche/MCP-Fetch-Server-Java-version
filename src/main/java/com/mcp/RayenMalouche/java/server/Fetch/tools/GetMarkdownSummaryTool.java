package com.mcp.RayenMalouche.java.server.Fetch.tools;

import com.mcp.RayenMalouche.java.server.Fetch.service.WebContentService;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

public class GetMarkdownSummaryTool extends BaseFetchTool {

    private final WebContentService webContentService;

    public GetMarkdownSummaryTool(WebContentService webContentService) {
        this.webContentService = webContentService;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification getToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(
                        "get_markdown_summary",
                        "Extracts and converts the main content area of a web page to Markdown format, " +
                                "automatically removing navigation menus, headers, footers, and other peripheral " +
                                "content. Perfect for capturing the core content of articles, blog posts, " +
                                "or documentation pages.",
                        createUrlInputSchema()
                ),
                (exchange, params) -> {
                    try {
                        String url = validateAndGetUrl(params);
                        String content = webContentService.getMarkdownSummary(url);
                        return createSuccessResult(content);
                    } catch (IllegalArgumentException e) {
                        return createErrorResult("Invalid parameters: " + e.getMessage());
                    } catch (Exception e) {
                        return createErrorResult("Failed to extract and convert main content: " + e.getMessage());
                    }
                }
        );
    }
}