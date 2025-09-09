package com.mcp.RayenMalouche.java.server.Fetch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.mcp.RayenMalouche.java.server.Fetch.tools.*;
import com.mcp.RayenMalouche.java.server.Fetch.service.WebContentService;

import java.util.List;

@SpringBootApplication
public class FetchApplication {
	private static final int MCP_SERVER_PORT = 45455;
	private static final int REST_SERVER_PORT = 8080;
	private static final String SERVER_NAME = "mcp-server-fetch-java";
	private static final String SERVER_VERSION = "1.0.0";

	public static void main(String[] args) throws Exception {
		// Start Spring Boot application for REST API
		ConfigurableApplicationContext springContext = SpringApplication.run(FetchApplication.class, args);
		System.out.println("REST API Server started on port " + REST_SERVER_PORT);
		System.out.println("Available endpoints:");
		System.out.println("  - Health: http://localhost:" + REST_SERVER_PORT + "/api/fetch/health");
		System.out.println("  - Endpoints: http://localhost:" + REST_SERVER_PORT + "/api/fetch/endpoints");
		System.out.println("  - Raw Text: http://localhost:" + REST_SERVER_PORT + "/api/fetch/raw-text");
		System.out.println("  - Rendered HTML: http://localhost:" + REST_SERVER_PORT + "/api/fetch/rendered-html");
		System.out.println("  - Markdown: http://localhost:" + REST_SERVER_PORT + "/api/fetch/markdown");
		System.out.println("  - Markdown Summary: http://localhost:" + REST_SERVER_PORT + "/api/fetch/markdown-summary");

		// Initialize web content service for MCP server
		WebContentService webContentService = new WebContentService();

		// Start MCP server in a separate thread
		Thread mcpServerThread = new Thread(() -> {
			try {
				startMcpServer(webContentService);
			} catch (Exception e) {
				System.err.println("Failed to start MCP server: " + e.getMessage());
				e.printStackTrace();
			}
		});
		mcpServerThread.setDaemon(true);
		mcpServerThread.start();

		System.out.println("MCP Server started on port " + MCP_SERVER_PORT);
		System.out.println("  - SSE Endpoint: http://localhost:" + MCP_SERVER_PORT + "/sse");

		// Keep the main thread alive
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Shutting down servers...");
			springContext.close();
			webContentService.cleanup();
		}));
	}

	private static void startMcpServer(WebContentService webContentService) throws Exception {
		// Create SSE transport provider
		HttpServletSseServerTransportProvider transportProvider =
				new HttpServletSseServerTransportProvider(new ObjectMapper(), "/", "/sse");

		// Build synchronous MCP server
		McpSyncServer syncServer = McpServer.sync(transportProvider)
				.serverInfo(SERVER_NAME, SERVER_VERSION)
				.capabilities(McpSchema.ServerCapabilities.builder()
						.tools(true)
						.resources(false, false)
						.prompts(false)
						.build())
				.build();

		// Register all tools
		registerTools(syncServer, webContentService);

		// Configure Jetty server
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setName("mcp-fetch-server");

		Server server = new Server(threadPool);
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(MCP_SERVER_PORT);
		server.addConnector(connector);

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.addServlet(new ServletHolder(transportProvider), "/*");
		server.setHandler(context);

		// Start server
		server.start();
		server.join();
	}

	private static void registerTools(McpSyncServer syncServer, WebContentService webContentService) {
		// Register get_raw_text tool
		syncServer.addTool(new GetRawTextTool(webContentService).getToolSpecification());

		// Register get_rendered_html tool
		syncServer.addTool(new GetRenderedHtmlTool(webContentService).getToolSpecification());

		// Register get_markdown tool
		syncServer.addTool(new GetMarkdownTool(webContentService).getToolSpecification());

		// Register get_markdown_summary tool
		syncServer.addTool(new GetMarkdownSummaryTool(webContentService).getToolSpecification());
	}
}