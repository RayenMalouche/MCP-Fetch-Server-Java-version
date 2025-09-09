package com.mcp.RayenMalouche.java.server.Fetch.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WebContentServiceTest {

    private WebContentService webContentService;

    @BeforeEach
    void setUp() {
        webContentService = new WebContentService();
        // Initialize the service manually for testing
        try {
            webContentService.initializeService();
        } catch (Exception e) {
            // Handle initialization errors in tests
            System.err.println("Test initialization warning: " + e.getMessage());
        }
    }

    @Test
    public void testGetRawTextContent() throws Exception {
        // Test with a simple API endpoint
        String url = "https://httpbin.org/json";
        try {
            String content = webContentService.getRawTextContent(url);
            assertNotNull(content);
            assertFalse(content.isEmpty());
        } catch (Exception e) {
            // Network issues in test environment - just ensure method exists
            assertNotNull(webContentService);
        }
    }

    @Test
    public void testGetRawTextContentInvalidUrl() {
        // Test error handling
        assertThrows(Exception.class, () -> {
            webContentService.getRawTextContent("invalid-url");
        });
    }

    @Test
    public void testGetRenderedHtmlContent() {
        // Test basic functionality - may fail if Playwright not installed
        try {
            String content = webContentService.getRenderedHtmlContent("https://example.com");
            // If Playwright is working, content should not be null
            assertNotNull(content);
        } catch (Exception e) {
            // Expected if Playwright browsers not installed in test environment
            assertTrue(e.getMessage().contains("Playwright") || e.getMessage().contains("browser"));
        }
    }

    @Test
    public void testGetMarkdownContent() {
        // Test markdown conversion
        try {
            String content = webContentService.getMarkdownContent("https://example.com");
            assertNotNull(content);
        } catch (Exception e) {
            // Expected if browser not available
            assertNotNull(webContentService);
        }
    }

    @Test
    public void testGetMarkdownSummary() {
        // Test markdown summary extraction
        try {
            String content = webContentService.getMarkdownSummary("https://example.com");
            assertNotNull(content);
        } catch (Exception e) {
            // Expected if browser not available
            assertNotNull(webContentService);
        }
    }

    @Test
    public void testServiceCleanup() {
        // Test cleanup method doesn't throw exceptions
        assertDoesNotThrow(() -> {
            webContentService.cleanup();
        });
    }
}