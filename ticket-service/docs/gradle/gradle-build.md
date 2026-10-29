# Gradle Build

How `ticket-service` is built, tested, and configured per environment. This is the only
module in the repo that uses Gradle (Kotlin DSL: `build.gradle.kts` /
`settings.gradle.kts`) — `analytics-service` is a separate Node/NestJS stack
with its own tooling.

## Toolchain

- JDK **25**, auto-provisioned via the `org.gradle.toolchains.foojay-resolver-convention`
  plugin (`settings.gradle.kts`) so a matching JDK is fetched automatically on any machine —
  CI runners included — without depending on whatever JDK happens to already be installed.
- Spring Boot **4.1.0**, dependency management via `io.spring.dependency-management`.

## Building the app

```bash
./gradlew compileJava compileTestJava   # compile only, no tests
./gradlew test                          # run the test suite (needs Docker, see below)
./gradlew build                         # compile, test, and package -- runs `test` first
./gradlew build -x test                 # package without running tests
./gradlew cleanTest test                # force a full test re-run instead of relying on
                                         # Gradle's up-to-date cache
./gradlew bootJar                       # package only, skip `build`'s other checks
```

Tests include Testcontainers-backed repository tests (Postgres) and a full Spring context
load (`TicketServiceApplicationTests`), so **Docker must be running** locally and in CI for
`./gradlew test` (and therefore plain `./gradlew build`) to pass.

`build`/`bootJar` produce an executable fat jar at
`build/libs/ticket-service-<version>.jar` (version from `version.properties`), filtered
against the `skaran` environment by default -- pass `-Penv=<name>` to build against a
different one (see [Environment-specific properties](#environment-specific-properties-resource-filtering)
below).

### Building with a specific environment's properties

`-Penv=<name>` picks which `environments/<name>.properties` file `processResources` filters
`application.properties` against. It's accepted by any task that depends on
`processResources` -- `build`, `bootJar`, `bootRun`, even a bare `processResources` run:

```bash
./gradlew build                 # default -- envName falls back to 'skaran' when -Penv is omitted
./gradlew build -Penv=skaran    # same thing, explicit

./gradlew build -Penv=prod      # fails today -- no environments/prod.properties yet
```

Right now **`skaran` is the only environment file that exists**
(`environments/skaran.properties`), so it's both the default and the only one that
currently works. To add another target, create `environments/<name>.properties` with the
same 10 keys `skaran.properties` has -- one for every `@token@` placeholder in
`application.properties` -- then build/run with `-Penv=<name>`.

## Running the app

### 1. Start the app's infrastructure dependencies

Keycloak (JWT issuer) and RabbitMQ are provided by the repo-root compose file:

```bash
docker compose -f ../infrastructure/docker-compose.yml up -d
```

This starts:

- **Keycloak** on `localhost:9090` (admin/admin), matching `skaran.properties`'
  `issuer-uri=http://localhost:9090/realms/event-ticket-platform`. The
  `event-ticket-platform` realm itself isn't provisioned by this compose file -- create it
  (and its client/users) in the Keycloak admin console before the app can validate JWTs.
- **RabbitMQ** on `localhost:5672` (AMQP) / `localhost:15672` (management UI), admin/admin.

Postgres is **not** in this compose file -- `skaran.properties` points at a standing
instance (`192.168.1.6:5432/ticketservice`), not a disposable container. Point your own
environment file (`environments/<name>.properties`, see below) at whatever Postgres
instance you use, and create the `ticketservice` database yourself; Liquibase migrates the
schema on startup (`spring.jpa.hibernate.ddl-auto=validate` -- the app itself never DDLs).

ClamAV is optional: `antivirus.clamav.enabled=false` and `antivirus.noop.enabled=true` by
default, so local runs skip real virus scanning of uploaded event images unless you flip
those two properties and point `antivirus.clamav.*` at a running ClamAV instance.

Stripe is optional too -- `stripe.api-key`/`stripe.webhook-secret` default to empty, which
is enough to boot the app; checkout/webhook endpoints just won't work until they're set
(see `src/main/resources/application-local.properties.example` for the local-profile
setup, issue #25).

### 2. Run the app

```bash
./gradlew bootRun                       # runs against environments/skaran.properties
./gradlew bootRun -Penv=<name>           # runs against a different environment file
```

or run the packaged jar directly:

```bash
./gradlew bootJar
java -jar build/libs/ticket-service-<version>.jar
```

To layer in the local Stripe secrets profile (optional, see step 1):

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

The app listens on **`http://localhost:4005/ticket-service`**
(`server.port`/`server.servlet.context-path` in `application.properties`) --
e.g. the Stripe webhook endpoint is
`http://localhost:4005/ticket-service/api/v1/payments/webhooks/stripe`.
Springdoc serves under that same base path:

- OpenAPI JSON: `http://localhost:4005/ticket-service/v3/api-docs`
- Swagger UI: `http://localhost:4005/ticket-service/swagger-ui/index.html`

## Dependencies of note

- `spring-boot-starter-webmvc`, `-data-jpa`, `-security`, `-security-oauth2-resource-server`,
  `-actuator`, `-liquibase`, `-quartz`, `-validation`, `-amqp`.
- `hibernate-spatial` — venue geography.
- `com.google.zxing` (`core` + `javase`) — QR code generation for tickets.
- `net.coobird:thumbnailator` — event image resizing.
- `xyz.capybara:clamav-client` — antivirus scanning of uploaded event images.
- `springdoc-openapi-starter-webmvc-ui` — pinned to the **3.x** line deliberately, since
  springdoc's major version tracks Spring Boot's (3.x → Boot 4; 2.x only supports Boot 3).
- `com.stripe:stripe-java` — Stripe Checkout + webhooks (issue #18).
- `com.google.code.gson` — added explicitly and pinned to the exact version `stripe-java`
  itself requires. `stripe-java` only exposes `gson` on its *runtime* classpath, not its
  compile/api one (deliberate on Stripe's part, so it doesn't leak as part of their public
  API), but `StripePaymentGatewayService` reads raw webhook JSON directly and needs it at
  compile time — hence the explicit dependency here.
- `org.postgresql:postgresql` — runtime only; the datasource driver.

Test-only: `spring-boot-starter-data-jpa-test`, `-security-oauth2-resource-server-test`,
`-security-test`, `-webmvc-test`, `spring-boot-testcontainers`, plus the Testcontainers
JUnit 5 + Postgres modules.

## Environment-specific properties (resource filtering)

`application.properties` carries `@token@`-style Ant placeholders for everything that's
genuinely environment-coupled:

```properties
spring.datasource.url=@spring.datasource.url@
spring.datasource.username=@spring.datasource.username@
spring.datasource.password=@spring.datasource.password@
spring.security.oauth2.resourceserver.jwt.issuer-uri=@spring.security.oauth2.resourceserver.jwt.issuer-uri@
spring.rabbitmq.host=@spring.rabbitmq.host@
spring.rabbitmq.port=@spring.rabbitmq.port@
spring.rabbitmq.username=@spring.rabbitmq.username@
spring.rabbitmq.password=@spring.rabbitmq.password@
antivirus.clamav.host=@antivirus.clamav.host@
antivirus.clamav.port=@antivirus.clamav.port@
```

These are filled in at build time by `processResources`, using Gradle's
`ReplaceTokens` ant filter:

```kotlin
val envName = findProperty("env")?.toString() ?: "skaran"
val envProps = Properties().apply {
    file("environments/$envName.properties").inputStream().use { load(it) }
}

tasks.named<ProcessResources>("processResources") {
    filesMatching("application.properties") {
        filter<ReplaceTokens>("tokens" to envProps.stringPropertyNames().associateWith(envProps::getProperty))
    }
}
```

- Default environment is **`skaran`** (local dev). Pick a different one with
  `-Penv=<name>`, which loads `environments/<name>.properties` instead.
- Every `@token@` in `application.properties` must have a matching key in the chosen
  environment file, and vice versa — a one-to-one correspondence. Adding a new
  environment-coupled property means updating both.
- `environments/*.properties` files (including `skaran.properties`) are **git-tracked**,
  plaintext credentials included. That's a deliberate, pre-existing stance in this repo —
  see the `.gitignore` comment under "Local secrets" — scoped narrowly to the real Stripe
  test-mode keys (`src/main/resources/application-local.properties`, issue #25) as the one
  genuinely sensitive exception. Don't assume newly-added `environments/` files inherit
  that same exemption without checking.

To verify substitution worked, inspect the filtered output rather than the source:

```bash
./gradlew processResources
grep '@.*@' build/resources/main/application.properties   # should print nothing
```

## Quartz job store

`spring.quartz.job-store-type=jdbc` with `spring.quartz.jdbc.initialize-schema=never` —
the Quartz schema is owned by `src/main/resources/db/quartz-changelog/`, a separate
Liquibase changelog from the app's own, so Quartz must never try to initialize it itself.
This backs `TicketOrderExpiryJob`/`TicketOrderExpirySweeper`, migrated off `@Scheduled`
specifically so scheduled jobs survive an app restart.

## Liquibase changelogs

`db.changelog-master.xml` uses `<includeAll path="changes" relativeToChangelogFile="true"/>`
so every changeset file dropped into `src/main/resources/db/changelog/changes/` is picked
up automatically — no per-file registration step to remember. Keep the `NNN-` numeric
prefix sequential; `includeAll` applies them in filename order.
