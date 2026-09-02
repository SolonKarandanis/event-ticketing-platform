package com.etp.ticketservice.domain.model.antivirus;

import lombok.Builder;
import lombok.Value;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Builder
@Value
public class ByteArrayVirusScannable implements VirusScannable{
    byte[] bytes;

    String originalFilename;

    long size;


    @Override
    public InputStream getInputStream() {
        return null;
    }
}
