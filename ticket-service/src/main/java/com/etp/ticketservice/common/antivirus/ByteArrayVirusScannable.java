package com.etp.ticketservice.common.antivirus;

import java.io.InputStream;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ByteArrayVirusScannable implements VirusScannable {
    byte[] bytes;

    String originalFilename;

    long size;

    @Override
    public InputStream getInputStream() {
        return null;
    }
}
