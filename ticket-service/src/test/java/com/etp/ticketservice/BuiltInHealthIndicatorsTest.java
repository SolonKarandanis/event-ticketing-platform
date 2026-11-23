package com.etp.ticketservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.etp.ticketservice.common.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// The database and RabbitMQ health checks are Spring Boot's own auto-configured
// indicators (DataSourceHealthIndicator, RabbitHealthIndicator) -- no custom ones. This pins
// that they're registered and actually report the dependency's state, called in-process
// so the per-component details (hidden from anonymous HTTP callers) can be asserted on.
//
// spring.rabbitmq.port=1 is a closed port, so the connection is refused immediately and
// "RabbitMQ unreachable" is deterministic whether or not a local broker happens to be
// running on 5672.
@SpringBootTest
@TestPropertySource(properties = "spring.rabbitmq.port=1")
class BuiltInHealthIndicatorsTest extends PostgresTestContainer {

    @Autowired
    @Qualifier("dbHealthContributor")
    private HealthIndicator db;

    @Autowired
    @Qualifier("rabbitHealthContributor")
    private HealthIndicator rabbit;

    @Test
    void databaseIndicatorReportsUpAgainstAReachablePostgres() {
        Health health = db.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("database", "PostgreSQL");
    }

    @Test
    void rabbitIndicatorReportsDownWhenTheBrokerIsUnreachable() {
        Health health = rabbit.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error");
    }
}
