package com.etp.ticketservice.config.antivirus;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
// import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
// @RefreshScope
@ConfigurationProperties(prefix = "antivirus.clamav")
@Component
public class ClamAvProperties {

    private static final long serialVersionUID = 1L;

    private String host = "localhost";

    private Integer port = 3310;

    private boolean enabled;
}
