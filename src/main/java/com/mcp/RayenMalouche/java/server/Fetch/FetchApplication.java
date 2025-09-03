package com.mcp.RayenMalouche.java.server.Fetch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
	private static final int SERVER_PORT = 45455;
	private static final String SERVER_NAME = "mcp-server-fetch-java";
	private static final String SERVER_VERSION = "1.0.0";

	public static void main(String[] args) throws Exception {
		// Initialize web content service
		WebContentService webContentService = new WebContentService();

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
		connector.setPort(SERVER_PORT);
		server.addConnector(connector);

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.addServlet(new ServletHolder(transportProvider), "/*");
		server.setHandler(context);

		// Start server
		server.start();
		System.out.println("MCP Fetch Server started on port " + SERVER_PORT);
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
