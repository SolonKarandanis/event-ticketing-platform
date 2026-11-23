package com.etp.migration;

import com.etp.ticketservice.config.LiquibaseConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Applies both Liquibase changelogs (app schema + Quartz job store) and exits. Run once per deploy,
 * before the app nodes roll, via {@code ./gradlew migrate -Penv=<name>}.
 *
 * <p>Deliberately outside {@code com.etp.ticketservice} so the app's component scan never picks it
 * up. Starts only a DataSource and {@link LiquibaseConfiguration}'s two beans -- no web server,
 * Quartz, RabbitMQ, ClamAV or Stripe. Runs off the same classpath as the app, so Liquibase records
 * the same {@code db/changelog/...} filenames the app would.
 */
@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration(DataSourceAutoConfiguration.class)
@Import(LiquibaseConfiguration.class)
public class MigrationApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MigrationApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        // Command-line args outrank application.properties, so this wins over an environment
        // that disables startup migrations (spring.liquibase.enabled=false).
        String[] withOverride = new String[args.length + 1];
        withOverride[0] = "--spring.liquibase.enabled=true";
        System.arraycopy(args, 0, withOverride, 1, args.length);
        // A failed changeset throws out of run(), which exits the JVM non-zero.
        System.exit(SpringApplication.exit(app.run(withOverride)));
    }
}
