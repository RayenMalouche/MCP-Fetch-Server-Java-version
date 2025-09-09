package com.mcp.RayenMalouche.java.server.Fetch;

import org.springframework.beans.factory.annotation.Value;
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

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class FetchApplication {

	@Value("${mcp.server.port:45455}")
	private int mcpServerPort;

	@Value("${server.port:8080}")
	private int restServerPort;

	@Value("${mcp.server.name:mcp-server-fetch-java}")
	private String serverName;

	@Value("${mcp.server.version:1.0.0}")
	private String serverVersion;

	private static ConfigurableApplicationContext springContext;
	private static FetchApplication instance;

	public static void main(String[] args) throws Exception {
		// Start Spring Boot application for REST API
		springContext = SpringApplication.run(FetchApplication.class, args);

		// Keep the main thread alive
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Shutting down servers...");
			if (springContext != null) {
				springContext.close();
			}
		}));
	}

	@PostConstruct
	public void startMcpServer() {
		instance = this;

		System.out.println("=== MCP FETCH SERVER STARTING ===");
		System.out.println("REST API Server started on port " + restServerPort);
		System.out.println("Available endpoints:");
		System.out.println("  - Health: http://localhost:" + restServerPort + "/api/fetch/health");
		System.out.println("  - Endpoints: http://localhost:" + restServerPort + "/api/fetch/endpoints");
		System.out.println("  - Raw Text: http://localhost:" + restServerPort + "/api/fetch/raw-text");
		System.out.println("  - Rendered HTML: http://localhost:" + restServerPort + "/api/fetch/rendered-html");
		System.out.println("  - Markdown: http://localhost:" + restServerPort + "/api/fetch/markdown");
		System.out.println("  - Markdown Summary: http://localhost:" + restServerPort + "/api/fetch/markdown-summary");

		// Start MCP server in a separate thread
		Thread mcpServerThread = new Thread(() -> {
			try {
				startMcpServerInternal();
			} catch (Exception e) {
				System.err.println("Failed to start MCP server: " + e.getMessage());
				e.printStackTrace();
			}
		});
		mcpServerThread.setDaemon(true);
		mcpServerThread.start();

		System.out.println("MCP Server starting on port " + mcpServerPort);
		System.out.println("  - SSE Endpoint: http://localhost:" + mcpServerPort + "/sse");
		System.out.println("  - Server Name: " + serverName);
		System.out.println("  - Server Version: " + serverVersion);
		System.out.println("=====================================");
	}

	private void startMcpServerInternal() throws Exception {
		// Initialize web content service for MCP server
		WebContentService webContentService = new WebContentService();

		// Create SSE transport provider
		HttpServletSseServerTransportProvider transportProvider =
				new HttpServletSseServerTransportProvider(new ObjectMapper(), "/", "/sse");

		// Build synchronous MCP server
		McpSyncServer syncServer = McpServer.sync(transportProvider)
				.serverInfo(serverName, serverVersion)
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
		connector.setPort(mcpServerPort);
		server.addConnector(connector);

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.addServlet(new ServletHolder(transportProvider), "/*");
		server.setHandler(context);

		// Start server
		server.start();
		System.out.println("MCP Server successfully started on port " + mcpServerPort);
		server.join();
	}

	private void registerTools(McpSyncServer syncServer, WebContentService webContentService) {
		// Register get_raw_text tool
		syncServer.addTool(new GetRawTextTool(webContentService).getToolSpecification());

		// Register get_rendered_html tool
		syncServer.addTool(new GetRenderedHtmlTool(webContentService).getToolSpecification());

		// Register get_markdown tool
		syncServer.addTool(new GetMarkdownTool(webContentService).getToolSpecification());

		// Register get_markdown_summary tool
		syncServer.addTool(new GetMarkdownSummaryTool(webContentService).getToolSpecification());

		System.out.println("MCP Tools registered: get_raw_text, get_rendered_html, get_markdown, get_markdown_summary");
	}
}