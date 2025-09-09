# MCP Fetch Server - Java Implementation

A Java-based Model Context Protocol (MCP) server that provides web content fetching and conversion capabilities. This is a Java port of the original [TypeScript MCP fetch server](https://github.com/tatn/mcp-server-fetch-typescript.git).

## Features

The server provides four main tools for web content processing:

1. **get_raw_text**: Retrieves raw text content directly from URLs without browser rendering
2. **get_rendered_html**: Fetches fully rendered HTML content using Playwright browser automation
3. **get_markdown**: Converts web page content to well-formatted Markdown
4. **get_markdown_summary**: Extracts main content area and converts to Markdown (removes navigation, headers, footers)

## Project Structure

```
src/main/java/com/mcp/RayenMalouche/java/server/Fetch/
├── FetchApplication.java               # Main application class
├── service/
│   └── WebContentService.java         # Core web content processing service
└── tools/
│   ├── BaseFetchTool.java              # Base class for all fetch tools
│   ├── GetRawTextTool.java             # Raw text fetching tool
│   ├── GetRenderedHtmlTool.java        # HTML rendering tool
│   ├── GetMarkdownTool.java            # Markdown conversion tool
│   └── GetMarkdownSummaryTool.java     # Main content extraction tool
└── controller/
    └── WebContentController.java       # REST Controller
```

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Internet connection (for downloading Playwright browsers)

## Installation

1. Clone the repository:
```bash
git clone https://github.com/RayenMalouche/MCP-Fetch-Server-Java-version
cd MCP-Fetch-Server-Java-version
```

2. Install Playwright browsers (required for rendered HTML functionality):

For windows: 

```bash
# Save the install-playwright.bat script and run it
install-playwright.bat
```
```bash
# Or manually
mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install"
```

3. Build the project:
```bash
mvn clean compile
```

4. Package the application:
```bash
mvn package
```

## Running the Server

### Development Mode
```bash
mvn spring-boot:run
```

### Production Mode
```bash
java -jar target/Fetch-0.0.1-SNAPSHOT.jar
```

The server will start on port **45455** by default.

## Configuration

You can modify the server configuration by editing the following constants in `FetchApplication.java`:

- `SERVER_PORT`: Change the port number (default: 45455)
- `SERVER_NAME`: Modify the server name
- `SERVER_VERSION`: Update the version string

## API Endpoints

The server exposes the following MCP tools via HTTP Server-Sent Events (SSE):

### get_raw_text
```json
{
  "name": "get_raw_text",
  "arguments": {
    "url": "https://example.com/data.json"
  }
}
```

### get_rendered_html
```json
{
  "name": "get_rendered_html",
  "arguments": {
    "url": "https://example.com"
  }
}
```

### get_markdown
```json
{
  "name": "get_markdown",
  "arguments": {
    "url": "https://example.com/article"
  }
}
```

### get_markdown_summary
```json
{
  "name": "get_markdown_summary",
  "arguments": {
    "url": "https://example.com/blog-post"
  }
}
```

## Dependencies

### Core Dependencies
- **Spring Boot**: Application framework
- **MCP SDK**: Model Context Protocol implementation
- **Jackson**: JSON processing
- **Jetty**: HTTP server

### Web Processing
- **Playwright**: Browser automation for rendered content
- **JSoup**: HTML parsing and manipulation
- **Flexmark**: HTML to Markdown conversion
- **Java HTTP Client**: Raw content fetching

## Error Handling

The server includes comprehensive error handling for:
- Invalid URLs
- Network timeouts
- Browser automation failures
- HTML parsing errors
- Markdown conversion issues

All errors are returned as MCP tool results with descriptive error messages.

## Performance Considerations

- **Raw text fetching**: Fast, direct HTTP requests
- **Rendered HTML**: Slower due to browser automation overhead
- **Markdown conversion**: Additional processing time for HTML parsing
- **Browser resources**: Playwright browsers are properly cleaned up after use

## Testing

Run the test suite:
```bash
mvn test
```

## Troubleshooting

### Playwright Issues
If you encounter Playwright browser issues:
```bash
mvn exec:java@install-playwright-browsers
```
or force reinstall:
```bash
mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install --force"
```

### Memory Issues
For large pages, increase JVM memory:
```bash
java -Xmx2g -jar target/Fetch-0.0.1-SNAPSHOT.jar
```

### Port Conflicts
Change the port in `FetchApplication.java` if 45455 is already in use.

## License

MIT License - See the original TypeScript implementation for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## Acknowledgments

This project is a Java port of the original TypeScript implementation by [tatn](https://github.com/tatn/mcp-server-fetch-typescript.git).