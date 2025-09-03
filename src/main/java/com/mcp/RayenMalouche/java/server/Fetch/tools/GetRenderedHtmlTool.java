package com.mcp.RayenMalouche.java.server.Fetch.tools;

import com.mcp.RayenMalouche.java.server.Fetch.service.WebContentService;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

public class GetRenderedHtmlTool extends BaseFetchTool {

    private final WebContentService webContentService;

    public GetRenderedHtmlTool(WebContentService webContentService) {
        this.webContentService = webContentService;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification getToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(
                        "get_rendered_html",
                        "Fetches fully rendered HTML content using a headless browser, including " +
                                "JavaScript-generated content. Essential for modern web applications, " +
                                "single-page applications (SPAs), or any content that requires client-side " +
                                "rendering to be complete.",
                        createUrlInputSchema()
                ),
                (exchange, params) -> {
                    try {
                        String url = validateAndGetUrl(params);
                        String content = webContentService.getRenderedHtmlContent(url);
                        return createSuccessResult(content);
                    } catch (IllegalArgumentException e) {
                        return createErrorResult("Invalid parameters: " + e.getMessage());
                    } catch (Exception e) {
                        return createErrorResult("Failed to fetch rendered HTML content: " + e.getMessage());
                    }
                }
        );
    }
}