package com.etp.ticketservice.domain.model.antivirus;

import lombok.*;

import java.io.Serializable;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class VirusScanResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Result result;

    @Singular
    private List<VirusScanResultFileList> infectedFiles;

    @Singular
    private List<VirusScanResultFileList> cleanFiles;

    @Singular
    private List<VirusScanResultFileList> errorFiles;
}
