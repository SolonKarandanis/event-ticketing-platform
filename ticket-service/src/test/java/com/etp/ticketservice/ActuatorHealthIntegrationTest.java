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
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.metrics.token=" + ActuatorHealthIntegrationTest.METRICS_TOKEN)
class ActuatorHealthIntegrationTest extends PostgresTestContainer {

    static final String METRICS_TOKEN = "test-metrics-token";

    @LocalServerPort private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> get(String path, String... headers) throws Exception {
        HttpRequest.Builder request =
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/ticket-service" + path))
                        .GET();
        if (headers.length > 0) {
            request.headers(headers);
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> scrape(String token) throws Exception {
        return get("/actuator/prometheus", "Authorization", "Bearer " + token);
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
    void prometheusWithTheTokenExposesJvmHttpAndHikariMetrics() throws Exception {
        get("/actuator/health"); // make sure at least one HTTP request has been timed

        HttpResponse<String> response = scrape(METRICS_TOKEN);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("jvm_memory_used_bytes")
                .contains("jvm_gc_")
                .contains("http_server_requests_seconds")
                .contains("hikaricp_connections")
                .contains("tomcat_");
    }

    @Test
    void prometheusWithoutATokenIsUnauthorized() throws Exception {
        assertThat(get("/actuator/prometheus").statusCode()).isEqualTo(401);
    }

    @Test
    void prometheusWithAWrongTokenIsUnauthorized() throws Exception {
        assertThat(scrape("not-the-token").statusCode()).isEqualTo(401);
    }

    // A Keycloak JWT is not the scraper token: user tokens get no metrics either. The
    // JWT chain never sees this path, so even a well-formed JWT is just a wrong token here.
    @Test
    void prometheusRejectsAJwtShapedBearerToken() throws Exception {
        assertThat(scrape("eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ4In0.c2ln").statusCode()).isEqualTo(401);
    }

    @Test
    void otherActuatorPathsStillRequireAuthentication() throws Exception {
        assertThat(get("/actuator/env").statusCode()).isEqualTo(401);
    }
}
