package com.etp.ticketservice.common.antivirus;

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
