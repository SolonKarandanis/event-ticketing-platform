package com.etp.ticketservice.config.antivirus;

import com.etp.ticketservice.domain.service.antivirus.AntivirusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;


@Component
public class AntivirusHealthIndicator implements HealthIndicator {

    public static final String AP_PING = "av.ping";

    @Autowired
    AntivirusService antivirusService;

    @Override
    public Health health() {
        final boolean result = antivirusService.ping();
        if (!result) {
            return Health.outOfService().withDetail(AP_PING, result).build();
        }
        return Health.up().build();
    }
}
