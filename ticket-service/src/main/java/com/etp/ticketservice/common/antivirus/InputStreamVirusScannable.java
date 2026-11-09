package com.etp.ticketservice.common.antivirus;

import java.io.InputStream;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class InputStreamVirusScannable implements VirusScannable {

    InputStream inputStream;

    String originalFilename;

    long size;
}
