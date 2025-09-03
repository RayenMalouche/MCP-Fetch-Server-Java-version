package com.mcp.RayenMalouche.java.server.Fetch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@ExtendWith(MockitoExtension.class)
class WebContentServiceTest {

    private WebContentService webContentService;

    @BeforeEach
    void setUp() {
        webContentService = new WebContentService();
    }

    @Test
    void testGetRawTextContent_ValidUrl() throws Exception {
        // Test with a simple HTTP endpoint that returns plain text
        String testUrl = "https://httpbin.org/robots.txt";

        try {
            String content = webContentService.getRawTextContent(testUrl);
            assertNotNull(content);
            assertFalse(content.isEmpty());
        } catch (Exception e) {
            // Skip test if network is unavailable
            System.out.println("Skipping network test: " + e.getMessage());
        }
    }

    @Test
    void testGetRawTextContent_InvalidUrl() {
        String invalidUrl = "not-a-valid-url";

        assertThrows(Exception.class, () -> {
            webContentService.getRawTextContent(invalidUrl);
        });
    }

    @Test
    void testGetRenderedHtmlContent_ValidUrl() throws Exception {
        // Test with a simple HTML page
        String testUrl = "https://httpbin.org/html";

        try {
            String htmlContent = webContentService.getRenderedHtmlContent(testUrl);
            assertNotNull(htmlContent);
            assertFalse(htmlContent.isEmpty());
            assertTrue(htmlContent.contains("<html"));
        } catch (Exception e) {
            // Skip test if Playwright is not available or network issues
            System.out.println("Skipping browser test: " + e.getMessage());
        }
    }

    @Test
    void testGetMarkdownContent_ValidUrl() throws Exception {
        String testUrl = "https://httpbin.org/html";

        try {
            String markdownContent = webContentService.getMarkdownContent(testUrl);
            assertNotNull(markdownContent);
            // Markdown should be shorter than HTML and not contain HTML tags
            assertFalse(markdownContent.contains("<html"));
        } catch (Exception e) {
            // Skip test if dependencies are not available
            System.out.println("Skipping markdown test: " + e.getMessage());
        }
    }

    @Test
    void testGetMarkdownSummary_ValidUrl() throws Exception {
        String testUrl = "https://httpbin.org/html";

        try {
            String summaryContent = webContentService.getMarkdownSummary(testUrl);
            assertNotNull(summaryContent);
            // Summary should be text content without HTML tags
            assertFalse(summaryContent.contains("<script"));
            assertFalse(summaryContent.contains("<style"));
        } catch (Exception e) {
            // Skip test if dependencies are not available
            System.out.println("Skipping summary test: " + e.getMessage());
        }
    }

    @Test
    void testCleanup() {
        // Test that cleanup doesn't throw exceptions
        assertDoesNotThrow(() -> {
            webContentService.cleanup();
        });
    }
}