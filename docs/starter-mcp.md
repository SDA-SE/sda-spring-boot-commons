# Starter MCP

The module `sda-commons-starter-mcp` provides a Spring Boot starter for implementing Model Context Protocol (MCP)
servers with integrated SDA SE security features including OIDC authentication and Open Policy Agent (OPA)
authorization.

Based on:

- `org.springframework.ai:spring-ai-starter-mcp-server-webmvc`
- `org.springframework.boot:spring-boot-starter-oauth2-resource-server`
- `org.springframework.security:spring-security-oauth2-jose`
- `org.springframework.boot:spring-boot-starter-oauth2-client`
- `org.springframework.boot:spring-boot-starter-actuator`
- `org.springframework.boot:spring-boot-starter-validation`
- `org.springframework.cloud:spring-cloud-starter-openfeign`
- `io.micrometer:micrometer-tracing-bridge-otel`
- `org.springdoc:springdoc-openapi-starter-webmvc-api`

## Configuration

--8<-- "doc-snippets/config-starter-mcp.md"

For further information about MCP protocol, have a look at
the [Model Context Protocol Specification](https://spec.modelcontextprotocol.io/).

**Please make sure to configure `spring.application.name` for every service.**

## What is MCP?

The Model Context Protocol (MCP) is an open standard that enables AI applications to securely connect to data sources
and tools. It provides a standardized way for AI systems to access external context and capabilities while maintaining
security and control.

Key features of MCP:

- **Standardized Protocol**: Uses JSON-RPC 2.0 for communication
- **Tool Integration**: Allows AI models to call external tools and functions
- **Resource Access**: Enables secure access to external data sources
- **Server-Sent Events**: Supports real-time communication via SSE

## MCP Server Implementation

### Basic Setup

To create an MCP server, annotate your Spring Boot application with `@SpringBootApplication` and define tools using the
`@Tool` annotation:

```java

@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider toolCallbackProvider(MyToolService toolService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(toolService)
                .build();
    }
}

@Service
public class MyToolService {

    @Tool(description = "Calculate the sum of two numbers")
    public int add(int a, int b) {
        return a + b;
    }

    @Tool(description = "Say hello to a given name or to the world")
    public String sayHello(String name) {
        return (name == null || name.isBlank()) ? "Hello, World!" : "Hello, " + name + "!";
    }
}
```

### Required Configuration

Configure the basic MCP server properties in your `application.properties`:

```properties
spring.application.name=my-mcp-server
spring.ai.mcp.server.name=my-mcp-server
spring.ai.mcp.server.version=1.0.0
spring.ai.mcp.server.type=sync
spring.ai.mcp.server.instructions=You are a helpful assistant.
spring.ai.mcp.server.capabilities.tool=true
```

### Server Endpoints

The MCP server automatically exposes the following endpoints:

- **SSE Endpoint**: `/sse` - Server-Sent Events for real-time communication
- **Message Endpoint**: `/mcp/message` - JSON-RPC message handling

These endpoints can be customized via configuration:

```properties
spring.ai.mcp.server.sse-endpoint=/custom-sse
spring.ai.mcp.server.sse-message-endpoint=/custom/message
```

## Authentication

The MCP starter provides OIDC-based authentication that integrates seamlessly with SDA SE authentication concepts.

### OIDC Configuration

OIDC Authentication can be configured with `auth.issuers` to provide a comma-separated list of trusted issuers:

```properties
auth.issuers=https://your-oidc-provider.com,https://another-provider.com
```

In development and test environments, authentication can be disabled:

```properties
auth.disable=true
```

!!! warning
Disabling authentication should only be used in development/test environments. Production deployments must always use
proper authentication.

### JWT Token Handling

The authentication system:

- Validates JWT tokens from configured issuers
- Updates JWKS cache when unknown Key IDs are received
- Refreshes JWKS cache every 5 minutes
- Invalidates cached JWK after 15 minutes
- Supports both authenticated and anonymous requests (decision made by OPA policies)

### Anonymous Mode

If no issuers are configured, the server runs in anonymous-only mode:

```java
// No auth.issuers configured
// Server allows only anonymous requests and rejects any JWT tokens
```

This is useful for internal services that don't require external authentication.

## Authorization with Open Policy Agent

The MCP starter integrates with [Open Policy Agent](https://www.openpolicyagent.org/) for fine-grained authorization
control.

### OPA Configuration

Configure OPA connection settings:

```properties
opa.base.url=http://localhost:8181
opa.policy.package=com.mycompany.mcp.server
opa.client.timeout=500ms
opa.client.connection.timeout=500ms
```

For development/testing, OPA can be disabled:

```properties
opa.disable=true
```

### OPA Request Input

The authorization system provides the following input to OPA policies:

| Property           | Description                                    | Example                                      |
|--------------------|------------------------------------------------|----------------------------------------------|
| `input.jwt`        | Validated encoded JWT as string (if available) | `"eyJhbGciOiJIUzI1NiJ9..."`                  |
| `input.path`       | Request path as array of path segments         | `["mcp", "message"]`                         |
| `input.httpMethod` | HTTP method as uppercase string                | `"POST"`                                     |
| `input.traceToken` | Trace-Token header value                       | `"abc-123-def"`                              |
| `input.body`       | Parsed JSON request body                       | `{"jsonrpc": "2.0", "method": "initialize"}` |

### Example OPA Policy

```python
package
com.mycompany.mcp.server

import input

# decode the token
token = {"payload": payload}
{
    not input.jwt == null
io.jwt.decode(input.jwt, [_, payload, _])
}

# deny by default
default
allow = false

# allow MCP initialize for authenticated users
allow
{
    input.httpMethod == "POST"
input.path = ["mcp", "message"]
input.body.method == "initialize"
token.payload.sub  # user must be authenticated
}

# allow tool calls for users with specific role
allow
{
    input.httpMethod == "POST"
input.path = ["mcp", "message"]
input.body.method == "tools/call"
token.payload.roles[_] == "mcp-user"
}

# allow SSE endpoint for authenticated users
allow
{
    input.httpMethod == "GET"
input.path = ["sse"]
token.payload.sub
}
```

### OPA Input Extensions

Custom input data can be provided to OPA policies by implementing [
`OpaInputExtension`](sda-commons-starter-mcp/src/main/java/org/sdase/commons/spring/boot/mcp/server/auth/opa/extension/OpaInputExtension.java:8):

```java

@Component
public class CustomOpaInputExtension implements OpaInputExtension<CustomData> {

    @Override
    public String getNamespace() {
        return "custom";
    }

    @Override
    public CustomData createAdditionalInputContent(HttpServletRequest request) {
        return new CustomData(
                request.getHeader("X-Custom-Header"),
                extractBusinessContext(request)
        );
    }
}
```

This will add the custom data under `input.custom` in OPA requests.

## Request Body Caching

The MCP starter automatically configures request body caching to allow multiple reads of the request body, which is
essential for:

- OPA authorization (reading body for policy decisions)
- MCP message processing
- Logging and debugging

The [
`RequestBodyCachingFilter`](sda-commons-starter-mcp/src/main/java/org/sdase/commons/spring/boot/mcp/server/filter/RequestBodyCachingFilter.java:15)
is automatically registered with highest precedence to ensure it runs before other filters.

## Security Considerations

### Authentication Security

- JWT tokens are validated against configured OIDC issuers
- Token signature verification uses JWKS from issuers
- No support for static local public keys (use OIDC issuers)
- Clock skew is fixed to 60 seconds
- IDP must provide `iss` claim matching discovery base URI

### Authorization Security

- All requests are subject to OPA policy evaluation
- Anonymous requests are allowed but must be explicitly authorized by policies
- Request body content is available to policies for fine-grained control
- Trace tokens are passed to policies for audit trails

### CORS Configuration

CORS is enabled by default with Spring Security defaults. For custom CORS configuration:

```java

@Configuration
public class CorsConfiguration {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://trusted-domain.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

## Error Handling

### MCP Protocol Errors

The server handles MCP protocol errors according to the JSON-RPC 2.0 specification:

- **Parse errors**: Invalid JSON in request
- **Invalid requests**: Missing required fields
- **Method not found**: Unknown MCP method
- **Invalid parameters**: Incorrect tool parameters
- **Internal errors**: Server-side exceptions

### Authentication Errors

- **401 Unauthorized**: Missing or invalid JWT token
- **403 Forbidden**: OPA policy denied access

### Tool Execution Errors

Tool exceptions are caught and returned as JSON-RPC error responses.

## Testing

### Integration Testing

If you want to ensure proper authentication and authorization in your integration tests, you can use the capabilities of
the `sda-commons-web-testing` module. You can use the [
`EnableSdaAuthMockInitializer`](../sda-commons-web-testing/src/main/java/org/sdase/commons/spring/boot/web/testing/auth/EnableSdaAuthMockInitializer.java)
to enable a mock authentication
server and the `AuthMock` to configure the expected authentication and authorization behavior. The WebClient is used
since the `io.modelcontextprotocol.client.McpClient` does not supply HTTP response code information.

```java

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = EnableSdaAuthMockInitializer.class)
@AutoConfigureWireMock
class McpServerIT {

    @Autowired
    AuthMock authMock;

    @Test
    void shouldCallToolWithAuthentication() {
        // Configure mock authentication
        authMock.authorizeRequest()
                .withHttpMethod("POST")
                .withPath("/mcp/message")
                .allow();

        String token = authMock.authentication().token();

        // Test MCP tool call with mocked authentication
        WebClient client = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        var response = client.post()
                .uri("/mcp/message")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(toolCallRequest)
                .retrieve()
                .toBodilessEntity()
                .block();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

If you want to test the MCP functionality by checking the correct output, it is more straightforward to use the
`io.modelcontextprotocol.client.McpClient`
directly. In this case, you can disable authentication and authorization for your tests.

```java

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"opa.disable=true", "auth.disable=true"})
class McpServerIT {

    @Autowired
    ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    @Test
    void shouldReturnHello() {
        withClient(
                client -> {
                    final var result =
                            client.callTool(new McpSchema.CallToolRequest("sayHello", Map.of("name", "World")));

                    result.content().stream()
                            .filter(item -> item instanceof McpSchema.TextContent)
                            .findFirst()
                            .ifPresent(
                                    item -> {
                                        McpSchema.TextContent textContent = (McpSchema.TextContent) item;
                                        assertThat(textContent.text()).isEqualTo("Hello, World!");
                                    });
                    assertThat(result.isError()).isFalse();
                });
    }

    private void withClient(Consumer<McpSyncClient> func) {
        final var transport =
                HttpClientSseClientTransport.builder("http://localhost:" + port)
                        .objectMapper(objectMapper)
                        .build();
        try (var mcpClient = McpClient.sync(transport).build()) {
            mcpClient.initialize();
            func.accept(mcpClient);
            mcpClient.closeGracefully();
        }
    }
}

```

## Migration from Other Implementations

### From Custom MCP Servers

If migrating from a custom MCP server implementation:

1. **Add dependency**: Include `sda-commons-starter-mcp` in your `build.gradle`
2. **Configure properties**: Set required MCP server properties
3. **Migrate tools**: Convert tool implementations to use `@Tool` annotation
4. **Update security**: Configure OIDC issuers and OPA policies
5. **Test integration**: Verify authentication and authorization work correctly

### From Plain Spring AI MCP

If migrating from plain Spring AI MCP server:

1. **Replace dependency**: Change from `spring-ai-starter-mcp-server-webmvc` to `sda-commons-starter-mcp`
2. **Add security configuration**: Configure authentication and authorization
3. **Update endpoints**: Ensure clients use the correct SSE and message endpoints
4. **Add monitoring**: Configure health checks and metrics

## Best Practices

### Tool Design

- **Idempotent operations**: Design tools to be safely retryable
- **Clear descriptions**: Provide detailed tool descriptions for AI understanding
- **Parameter validation**: Validate tool parameters and provide clear error messages
- **Resource management**: Properly manage external resources and connections

### Security

- **Principle of least privilege**: OPA policies should grant minimal necessary access
- **Input validation**: Validate all tool inputs, especially from untrusted sources
- **Audit logging**: Log tool executions for security and debugging
- **Rate limiting**: Consider implementing rate limiting for resource-intensive tools

### Performance

- **Async operations**: Use async processing for long-running tools when possible
- **Connection pooling**: Use connection pools for external service calls
- **Caching**: Cache expensive computations where appropriate
- **Resource limits**: Set appropriate timeouts and resource limits

## Known Issues

- There is currently (Spring AI Version: 1.0.2) an issue (probably serialization related) where the String responses to
  Tool calls are wrapped into an additional pair of quotes.
- In comparison to the authorization solution in the `sda-commons-starter-web` module, the OPA integration in this
  module does not yet support the mapping of custom constraints into Java objects, since there is no request context
  available. This is to be expected in a future release of Spring AI.