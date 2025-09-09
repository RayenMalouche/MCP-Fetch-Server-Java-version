package com.mcp.RayenMalouche.java.server.Fetch.controller;

import com.mcp.RayenMalouche.java.server.Fetch.service.WebContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/fetch")
@CrossOrigin(origins = "*")
public class WebContentController {

    @Autowired
    private WebContentService webContentService;

    /**
     * Get raw text content from a URL
     */
    @PostMapping("/raw-text")
    public ResponseEntity<Map<String, Object>> getRawText(@RequestBody Map<String, String> request) {
        return handleRequest(() -> {
            String url = validateUrl(request.get("url"));
            return webContentService.getRawTextContent(url);
        });
    }

    /**
     * Get rendered HTML content from a URL
     */
    @PostMapping("/rendered-html")
    public ResponseEntity<Map<String, Object>> getRenderedHtml(@RequestBody Map<String, String> request) {
        return handleRequest(() -> {
            String url = validateUrl(request.get("url"));
            return webContentService.getRenderedHtmlContent(url);
        });
    }

    /**
     * Get markdown content from a URL
     */
    @PostMapping("/markdown")
    public ResponseEntity<Map<String, Object>> getMarkdown(@RequestBody Map<String, String> request) {
        return handleRequest(() -> {
            String url = validateUrl(request.get("url"));
            return webContentService.getMarkdownContent(url);
        });
    }

    /**
     * Get markdown summary (main content only) from a URL
     */
    @PostMapping("/markdown-summary")
    public ResponseEntity<Map<String, Object>> getMarkdownSummary(@RequestBody Map<String, String> request) {
        return handleRequest(() -> {
            String url = validateUrl(request.get("url"));
            return webContentService.getMarkdownSummary(url);
        });
    }

    /**
     * GET endpoint for raw text (alternative method)
     */
    @GetMapping("/raw-text")
    public ResponseEntity<Map<String, Object>> getRawTextGet(@RequestParam String url) {
        return handleRequest(() -> {
            String validatedUrl = validateUrl(url);
            return webContentService.getRawTextContent(validatedUrl);
        });
    }

    /**
     * GET endpoint for rendered HTML (alternative method)
     */
    @GetMapping("/rendered-html")
    public ResponseEntity<Map<String, Object>> getRenderedHtmlGet(@RequestParam String url) {
        return handleRequest(() -> {
            String validatedUrl = validateUrl(url);
            return webContentService.getRenderedHtmlContent(validatedUrl);
        });
    }

    /**
     * GET endpoint for markdown (alternative method)
     */
    @GetMapping("/markdown")
    public ResponseEntity<Map<String, Object>> getMarkdownGet(@RequestParam String url) {
        return handleRequest(() -> {
            String validatedUrl = validateUrl(url);
            return webContentService.getMarkdownContent(validatedUrl);
        });
    }

    /**
     * GET endpoint for markdown summary (alternative method)
     */
    @GetMapping("/markdown-summary")
    public ResponseEntity<Map<String, Object>> getMarkdownSummaryGet(@RequestParam String url) {
        return handleRequest(() -> {
            String validatedUrl = validateUrl(url);
            return webContentService.getMarkdownSummary(validatedUrl);
        });
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "mcp-fetch-server");
        return ResponseEntity.ok(response);
    }

    /**
     * Get available endpoints
     */
    @GetMapping("/endpoints")
    public ResponseEntity<Map<String, Object>> getEndpoints() {
        Map<String, Object> response = new HashMap<>();
        response.put("endpoints", Map.of(
                "raw-text", Map.of(
                        "description", "Retrieves raw text content directly from a URL without browser rendering",
                        "methods", new String[]{"GET", "POST"},
                        "get_example", "/api/fetch/raw-text?url=https://example.com",
                        "post_example", "{\"url\": \"https://example.com\"}"
                ),
                "rendered-html", Map.of(
                        "description", "Fetches fully rendered HTML content using a headless browser",
                        "methods", new String[]{"GET", "POST"},
                        "get_example", "/api/fetch/rendered-html?url=https://example.com",
                        "post_example", "{\"url\": \"https://example.com\"}"
                ),
                "markdown", Map.of(
                        "description", "Converts web page content to well-formatted Markdown",
                        "methods", new String[]{"GET", "POST"},
                        "get_example", "/api/fetch/markdown?url=https://example.com",
                        "post_example", "{\"url\": \"https://example.com\"}"
                ),
                "markdown-summary", Map.of(
                        "description", "Extracts and converts the main content area to Markdown format",
                        "methods", new String[]{"GET", "POST"},
                        "get_example", "/api/fetch/markdown-summary?url=https://example.com",
                        "post_example", "{\"url\": \"https://example.com\"}"
                )
        ));
        return ResponseEntity.ok(response);
    }

    private String validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL parameter is required");
        }

        String trimmedUrl = url.trim();
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            throw new IllegalArgumentException("URL must start with http:// or https://");
        }

        return trimmedUrl;
    }

    private ResponseEntity<Map<String, Object>> handleRequest(ContentFetcher fetcher) {
        Map<String, Object> response = new HashMap<>();

        try {
            String content = fetcher.fetch();
            response.put("success", true);
            response.put("content", content);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", "Invalid parameters: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to fetch content: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @FunctionalInterface
    private interface ContentFetcher {
        String fetch() throws Exception;
    }
}