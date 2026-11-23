package com.etp.ticketservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.etp.ticketservice.common.PostgresTestContainer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

// Real HTTP over a random port, same setup as OpenApiDocsIntegrationTest (see its comment
// for why plain java.net.http.HttpClient) -- this is about what an unauthenticated probe
// actually sees through the full security filter chain, so a MockMvc slice isn't enough.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ActuatorHealthIntegrationTest extends PostgresTestContainer {

    @LocalServerPort private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/ticket-service" + path))
                        .GET()
                        .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void livenessIsPublicAndUp() throws Exception {
        HttpResponse<String> response = get("/actuator/health/liveness");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }

    @Test
    void readinessIsPublicAndUpWithTheDatabaseReachable() throws Exception {
        HttpResponse<String> response = get("/actuator/health/readiness");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }

    // The aggregate endpoint may report DOWN in tests (no RabbitMQ is running), so this
    // only asserts it's reachable without a token and doesn't leak component details.
    @Test
    void aggregateHealthIsPublicButHidesComponentDetails() throws Exception {
        HttpResponse<String> response = get("/actuator/health");

        assertThat(response.statusCode()).isNotIn(401, 403);
        assertThat(response.body()).contains("\"status\"").doesNotContain("components");
    }

    @Test
    void otherActuatorPathsStillRequireAuthentication() throws Exception {
        assertThat(get("/actuator/env").statusCode()).isEqualTo(401);
    }
}
