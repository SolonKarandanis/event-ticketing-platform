package com.etp.ticketservice.common.antivirus;

import lombok.Builder;
import lombok.Value;

import java.io.InputStream;

@Builder
@Value
public class InputStreamVirusScannable implements VirusScannable{

    InputStream inputStream;

    String originalFilename;

    long size;
}
