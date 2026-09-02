package com.etp.ticketservice.domain.service.antivirus;

import com.etp.ticketservice.domain.model.antivirus.VirusScanResult;
import com.etp.ticketservice.domain.model.antivirus.VirusScannable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "antivirus.noop.enabled", havingValue = "true")
public class AntivirusServiceNoopImpl extends AbstractAntivirusServiceImpl{

    @Override
    protected boolean doPing() {
        return true;
    }

    @Override
    protected Map<String, Collection<String>> doScan(VirusScannable scannable) {
        return new HashMap<String, Collection<String>>();
    }

    @Override
    protected VirusScanResult processScanResult(VirusScanResult scanResult) {
        return scanResult;
    }
}
