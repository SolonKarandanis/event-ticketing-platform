package com.etp.ticketservice.domain.model.antivirus;

import java.io.InputStream;

public interface VirusScannable {

    InputStream getInputStream();

    /**
     * @return the original file name
     */
    String getOriginalFilename();

    /**
     * Return the size of the file in bytes.
     *
     * @return the size of the file, or 0 if empty
     */
    long getSize();
}
