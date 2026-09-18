package com.etp.ticketservice.common.antivirus;

import lombok.*;

import java.io.Serializable;
import java.util.Collection;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class VirusScanResultFileList implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileName;

    private Result result;

    @Singular
    private Collection<VirusScanResultFile> scanFiles;
}
