package com.etp.ticketservice;

import com.etp.ticketservice.common.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Extends PostgresTestContainer so the full app context's Liquibase run connects to a
// real, disposable Postgres/PostGIS container instead of application.properties's
// hardcoded developer-machine datasource URL, which nothing outside that one LAN can
// reach (this is what was failing this test in CI -- see PostgresTestContainer's comment).
@SpringBootTest
class TicketServiceApplicationTests extends PostgresTestContainer {

    @Test
    void contextLoads() {
    }

}
