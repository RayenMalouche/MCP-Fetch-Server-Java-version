// Add to a new file: WebContentServiceTests.java
package com.mcp.RayenMalouche.java.server.Fetch;

import com.mcp.RayenMalouche.java.server.Fetch.service.WebContentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebContentServiceTests {

    private final WebContentService service = new WebContentService();

    @Test
    void testGetRawTextContent() throws Exception {
        String content = service.getRawTextContent("https://example.com");
        assertNotNull(content);
        assertTrue(content.contains("Example Domain"));  // Basic assertion on expected content
    }

    @Test
    void testGetMarkdownContent() throws Exception {
        String markdown = service.getMarkdownContent("https://example.com");
        assertNotNull(markdown);
        assertTrue(markdown.contains("# Example Domain"));  // Check for Markdown conversion
    }

    @Test
    void testInvalidUrl() {
        assertThrows(Exception.class, () -> service.getRawTextContent("invalid-url"));
    }
}