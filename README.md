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
src/main/java/com/dicovery/mcp/fetch/server/
├── FetchServerApplication.java          # Main application class
├── service/
│   └── WebContentService.java           # Core web content processing service
└── tools/
    ├── BaseFetchTool.java               # Base class for all fetch tools
    ├── GetRawTextTool.java              # Raw text fetching tool
    ├── GetRenderedHtmlTool.java         # HTML rendering tool
    ├── GetMarkdownTool.java             # Markdown conversion tool
    └── GetMarkdownSummaryTool.java      # Main content extraction tool
```

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Internet connection (for downloading Playwright browsers)

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd mcp-fetch-server-java
```

2. Build the project:
```bash
mvn clean compile
```

3. Install Playwright browsers (required for rendered HTML functionality):
```bash
mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install"
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
java -jar target/fetch-server-1.0.0.jar
```

The server will start on port **45451** by default.

## Configuration

You can modify the server configuration by editing the following constants in `FetchServerApplication.java`:

- `SERVER_PORT`: Change the port number (default: 45451)
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
- **Remark**: HTML to Markdown conversion
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
mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install --force"
```

### Memory Issues
For large pages, increase JVM memory:
```bash
java -Xmx2g -jar target/fetch-server-1.0.0.jar
```

### Port Conflicts
Change the port in `FetchServerApplication.java` if 45451 is already in use.

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