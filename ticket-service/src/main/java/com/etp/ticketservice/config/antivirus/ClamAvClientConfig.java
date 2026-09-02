package com.etp.ticketservice.config.antivirus;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.extern.slf4j.Slf4j;
import xyz.capybara.clamav.ClamavClient;

@Slf4j
@EnableConfigurationProperties
@Configuration
@ConditionalOnProperty(name = "antivirus.clamav.enabled", havingValue = "true")
public class ClamAvClientConfig {

    @Primary
    @Bean
    ClamavClient getClamavClient(@Autowired ClamAvProperties clamAvProps) {
        log.debug("configuring ClamAV client with: {}", clamAvProps);
        final ClamavClient avClient = new ClamavClient(clamAvProps.getHost(), clamAvProps.getPort());
        avClient.ping();
        return new ClamavClient(clamAvProps.getHost(), clamAvProps.getPort());
    }
}
