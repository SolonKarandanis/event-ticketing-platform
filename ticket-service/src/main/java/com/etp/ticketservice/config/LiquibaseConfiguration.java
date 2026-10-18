package com.etp.ticketservice.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

// Spring Boot's LiquibaseAutoConfiguration guards its own main-changelog SpringLiquibase
// bean behind @ConditionalOnMissingBean(SpringLiquibase.class) at the whole-nested-config
// level -- defining any second SpringLiquibase bean (quartzLiquibase below) silently
// disables Boot's bean entirely, not just lets ours coexist alongside it. So the app's own
// changelog bean is hand-rolled here too (still driven by the same spring.liquibase.change-log/
// default-schema properties Boot's own bean would have read -- @DataJpaTest slices exclude
// this whole class and fall back to Boot's bean, so those properties have to stay the one
// source of truth either way; see application.properties). Quartz's own JDBC job store
// tables get a second bean under its own changelog/changelog-tracking-table pair -- same
// reasoning as a Camunda-style liquibase-maven-plugin profile with its own
// databaseChangeLogTableName, just expressed as an explicit bean since this project has no
// Maven/Liquibase build step.
@Configuration
public class LiquibaseConfiguration {

    @Bean
    public SpringLiquibase liquibase(
            DataSource dataSource,
            @Value("${spring.liquibase.change-log}") String changeLog,
            @Value("${spring.liquibase.default-schema}") String defaultSchema) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setDefaultSchema(defaultSchema);
        return liquibase;
    }

    @Bean
    public SpringLiquibase quartzLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/quartz-changelog/db.changelog-quartz.xml");
        liquibase.setDefaultSchema("public");
        liquibase.setDatabaseChangeLogTable("quartz_databasechangelog");
        liquibase.setDatabaseChangeLogLockTable("quartz_databasechangeloglock");
        return liquibase;
    }
}
