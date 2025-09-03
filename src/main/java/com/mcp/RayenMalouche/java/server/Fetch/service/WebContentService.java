package com.mcp.RayenMalouche.java.server.Fetch.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.microsoft.playwright.*;
import com.overzealous.remark.Remark;

@Service
public class WebContentService {

    private static final int TIMEOUT_SECONDS = 20;
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(TIMEOUT_SECONDS);

    private final HttpClient httpClient;
    private final Remark remarkConverter;
    private Playwright playwright;
    private BrowserType browserType;

    public WebContentService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(HTTP_TIMEOUT)
                .build();

        this.remarkConverter = new Remark();

        // Initialize Playwright for browser operations
        initializePlaywright();
    }

    private void initializePlaywright() {
        try {
            this.playwright = Playwright.create();
            this.browserType = playwright.chromium();
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize Playwright. Browser-based operations will fail.");
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Fetches raw text content from a URL using HTTP client
     */
    public String getRawTextContent(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(HTTP_TIMEOUT)
                .header("User-Agent", "Mozilla/5.0 (compatible; MCP-Fetch-Server/1.0)")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new IOException("HTTP " + response.statusCode() + " error fetching URL: " + url);
        }
    }

    /**
     * Fetches fully rendered HTML content using Playwright browser
     */
    public String getRenderedHtmlContent(String url) throws Exception {
        if (playwright == null || browserType == null) {
            throw new Exception("Playwright not initialized. Cannot fetch rendered content.");
        }

        Browser browser = null;
        Page page = null;

        try {
            browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            page = context.newPage();

            // Navigate and wait for content to load
            page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(TIMEOUT_SECONDS * 1000));

            return page.content();

        } catch (Exception e) {
            System.err.println("Failed to fetch HTML for " + url + ": " + e.getMessage());
            return "";
        } finally {
            if (page != null) {
                try {
                    page.close();
                } catch (Exception e) {
                    System.err.println("Error closing page: " + e.getMessage());
                }
            }
            if (browser != null) {
                try {
                    browser.close();
                } catch (Exception e) {
                    System.err.println("Error closing browser: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Converts HTML content to Markdown format
     */
    public String getMarkdownContent(String url) throws Exception {
        String htmlContent = getRenderedHtmlContent(url);
        return convertHtmlToMarkdown(htmlContent, false);
    }

    /**
     * Extracts main content and converts to Markdown
     */
    public String getMarkdownSummary(String url) throws Exception {
        String htmlContent = getRenderedHtmlContent(url);
        return convertHtmlToMarkdown(htmlContent, true);
    }

    private String convertHtmlToMarkdown(String htmlContent, boolean mainContentOnly) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return "";
        }

        try {
            Document doc = Jsoup.parse(htmlContent);

            // Remove script and style elements
            doc.select("script").remove();
            doc.select("style").remove();

            if (mainContentOnly) {
                // Remove navigation, header, footer elements
                doc.select("header, footer, nav").remove();

                // Try to extract main content area
                Element mainContent = extractMainContent(doc);
                if (mainContent != null) {
                    doc = Jsoup.parse(mainContent.outerHtml());
                }
            }

            // Handle tables manually for better formatting
            Elements tables = doc.select("table");
            for (Element table : tables) {
                String tableMarkdown = convertTableToMarkdown(table);
                table.replaceWith(Jsoup.parse(tableMarkdown).body());
            }

            // Handle definition lists
            Elements dlLists = doc.select("dl");
            for (Element dl : dlLists) {
                String dlMarkdown = convertDefinitionListToMarkdown(dl);
                dl.replaceWith(Jsoup.parse(dlMarkdown).body());
            }

            return remarkConverter.convert(doc.html());

        } catch (Exception e) {
            System.err.println("Error converting HTML to Markdown: " + e.getMessage());
            return htmlContent; // Return original HTML if conversion fails
        }
    }

    private Element extractMainContent(Document doc) {
        // Try various selectors for main content
        String[] selectors = {
                "main", "article", "[role=main]", "#main", "#content",
                ".main", ".content", ".article", ".post", ".entry"
        };

        for (String selector : selectors) {
            Element element = doc.selectFirst(selector);
            if (element != null && !element.text().trim().isEmpty()) {
                return element;
            }
        }

        // Fallback: return body if no main content found
        return doc.body();
    }

    private String convertTableToMarkdown(Element table) {
        StringBuilder markdown = new StringBuilder("\n\n");
        Elements rows = table.select("tr");

        if (rows.isEmpty()) {
            return "";
        }

        // Process header row
        Element headerRow = rows.first();
        Elements headerCells = headerRow.select("th, td");

        if (!headerCells.isEmpty()) {
            markdown.append("|");
            for (Element cell : headerCells) {
                markdown.append(" ").append(cell.text().trim()).append(" |");
            }
            markdown.append("\n|");
            for (int i = 0; i < headerCells.size(); i++) {
                markdown.append("---|");
            }
            markdown.append("\n");

            // Process remaining rows
            for (int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cells = row.select("th, td");
                markdown.append("|");
                for (Element cell : cells) {
                    markdown.append(" ").append(cell.text().trim()).append(" |");
                }
                markdown.append("\n");
            }
        }

        return markdown.append("\n").toString();
    }

    private String convertDefinitionListToMarkdown(Element dl) {
        StringBuilder markdown = new StringBuilder("\n\n");
        Elements children = dl.children();

        String currentDt = "";
        for (Element child : children) {
            if (child.tagName().equals("dt")) {
                currentDt = child.text().trim();
                if (!currentDt.isEmpty()) {
                    markdown.append("**").append(currentDt).append(":** ");
                }
            } else if (child.tagName().equals("dd")) {
                String ddContent = child.text().trim();
                if (!ddContent.isEmpty()) {
                    markdown.append(ddContent).append("\n");
                }
            }
        }

        return markdown.append("\n").toString();
    }

    public void cleanup() {
        if (playwright != null) {
            try {
                playwright.close();
            } catch (Exception e) {
                System.err.println("Error closing Playwright: " + e.getMessage());
            }
        }
    }
}
