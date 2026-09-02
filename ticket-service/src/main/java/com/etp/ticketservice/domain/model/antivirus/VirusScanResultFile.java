package com.etp.ticketservice.domain.model.antivirus;

import lombok.*;

import java.io.Serializable;
import java.util.Collection;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class VirusScanResultFile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileAlias;

    @Singular
    private Collection<String> viruses;
}
