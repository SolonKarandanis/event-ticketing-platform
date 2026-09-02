package com.etp.ticketservice.domain.service.antivirus;

import com.etp.ticketservice.domain.model.antivirus.VirusScanResult;
import com.etp.ticketservice.domain.model.antivirus.VirusScannable;

import java.util.Collection;

public interface AntivirusService {
    /**
     * ping the anti-virus service (to be used for health-checks)
     *
     * @return
     */
    boolean ping();

    /**
     * @param scannables
     * @return
     */
    VirusScanResult scan(Collection<VirusScannable> scannables);

    VirusScanResult scan(VirusScannable scannable);
}
