package com.mcp.RayenMalouche.java.server.Fetch.tools;

import com.mcp.RayenMalouche.java.server.Fetch.service.WebContentService;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

public class GetRawTextTool extends BaseFetchTool {

    private final WebContentService webContentService;

    public GetRawTextTool(WebContentService webContentService) {
        this.webContentService = webContentService;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification getToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(
                        "get_raw_text",
                        "Retrieves raw text content directly from a URL without browser rendering. " +
                                "Ideal for structured data formats like JSON, XML, CSV, TSV, or plain text files. " +
                                "Best used when fast, direct access to the source content is needed without " +
                                "processing dynamic elements.",
                        createUrlInputSchema()
                ),
                (exchange, params) -> {
                    try {
                        String url = validateAndGetUrl(params);
                        String content = webContentService.getRawTextContent(url);
                        return createSuccessResult(content);
                    } catch (IllegalArgumentException e) {
                        return createErrorResult("Invalid parameters: " + e.getMessage());
                    } catch (Exception e) {
                        return createErrorResult("Failed to fetch raw text content: " + e.getMessage());
                    }
                }
        );
    }
}