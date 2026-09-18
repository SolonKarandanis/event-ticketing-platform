package com.etp.ticketservice;

import com.etp.ticketservice.common.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

// RANDOM_PORT (unlike the bare @SpringBootTest in TicketServiceApplicationTests, whose
// webEnvironment defaults to MOCK and never binds a real port) so this actually exercises
// springdoc's generated document and Swagger UI over real HTTP, not just confirms the
// beans construct without error. Plain java.net.http.HttpClient rather than
// TestRestTemplate -- this project has no generic spring-boot-starter-test bundle (every
// other test dependency here is one of Boot 4's split per-feature test starters), and
// pulling in whichever split module owns TestRestTemplate isn't worth it for one test
// class when the JDK's own client already does the job.
// Extends PostgresTestContainer for the same reason TicketServiceApplicationTests does --
// the full app context's Liquibase run needs a real, reachable Postgres, and
// application.properties's hardcoded datasource URL is a developer-machine LAN address.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiDocsIntegrationTest extends PostgresTestContainer {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void apiDocsAreServedPubliclyAndListKnownEndpoints() throws Exception {
        HttpResponse<String> response = get("/ticket-service/v3/api-docs");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"openapi\"");
        // A known, stable path -- confirms springdoc picked up the real controllers,
        // not just that the endpoint returned *some* JSON.
        assertThat(response.body()).contains("/api/v1/events");
    }

    @Test
    void swaggerUiIsServedPublicly() throws Exception {
        HttpResponse<String> response = get("/ticket-service/swagger-ui/index.html");

        assertThat(response.statusCode()).isEqualTo(200);
    }
}
