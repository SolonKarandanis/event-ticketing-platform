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

// Browser CORS preflights over real HTTP, through the full security filter chain. The test
// classpath carries the default (skaran) environment's filtered application.properties, so
// the allowed origin here is http://localhost:3000.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CorsIntegrationTest extends PostgresTestContainer {

    @LocalServerPort private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> preflight(String origin) throws Exception {
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "http://localhost:"
                                                + port
                                                + "/ticket-service/api/v1/published-events"))
                        .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                        .header("Origin", origin)
                        .header("Access-Control-Request-Method", "GET")
                        .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void preflightFromTheConfiguredOriginIsAllowed() throws Exception {
        HttpResponse<String> response = preflight("http://localhost:3000");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Access-Control-Allow-Origin"))
                .contains("http://localhost:3000");
    }

    @Test
    void preflightFromAnotherOriginIsRejected() throws Exception {
        HttpResponse<String> response = preflight("https://evil.example");

        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).isEmpty();
    }
}
