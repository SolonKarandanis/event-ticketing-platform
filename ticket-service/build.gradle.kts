import java.util.Properties
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
}

// Load version from version.properties
val versionProps = Properties().apply {
    file("version.properties").inputStream().use { load(it) }
}

group = "com.etp"
version = versionProps.getProperty("application.version")
description = "ticket-service"

// Per-environment values (datasource, oauth2 issuer, rabbitmq, clamav) filtered into
// application.properties' @token@ placeholders at build time -- see environments/.
// Pass -Penv=<name> to pick a file other than the local dev default (skaran).
val envName = findProperty("env")?.toString() ?: "skaran"
val envFile = file("environments/$envName.properties")
val envProps = Properties().apply {
    envFile.inputStream().use { load(it) }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.google.zxing:core:3.5.1")
    implementation("com.google.zxing:javase:3.5.1")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.hibernate.orm:hibernate-spatial")
    implementation("net.coobird:thumbnailator:0.4.20")
    implementation("xyz.capybara:clamav-client:2.1.2")
    // 3.x is the line that targets Spring Boot 4 (springdoc's major versions track
    // Spring Boot's own) -- the 2.x line only supports Boot 3.
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")
    implementation("com.stripe:stripe-java:34.0.0")
    // stripe-java only exposes gson on its runtime classpath, not its compile/api one
    // (deliberately, so it doesn't leak as part of stripe-java's own public API) -- our
    // own code reads raw webhook JSON directly (StripePaymentGatewayService), so it
    // needs gson as a real compile-time dependency here, pinned to the exact version
    // stripe-java itself requires.
    implementation("com.google.code.gson:gson:2.10.1")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named<ProcessResources>("processResources") {
    // ReplaceTokens' token values aren't tracked as task inputs on their own, so without
    // this, editing the environment file leaves processResources "up-to-date" and the
    // jar keeps the stale values.
    inputs.file(envFile)
    filesMatching("application.properties") {
        filter<ReplaceTokens>("tokens" to envProps.stringPropertyNames().associateWith(envProps::getProperty))
    }
}
