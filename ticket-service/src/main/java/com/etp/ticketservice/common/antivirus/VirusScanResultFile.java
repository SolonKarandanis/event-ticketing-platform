package com.etp.ticketservice.common.antivirus;

import java.io.Serializable;
import java.util.Collection;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class VirusScanResultFile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileAlias;

    @Singular private Collection<String> viruses;
}
