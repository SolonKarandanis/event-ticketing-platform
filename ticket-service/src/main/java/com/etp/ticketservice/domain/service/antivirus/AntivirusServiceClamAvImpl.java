package com.etp.ticketservice.domain.service.antivirus;

import com.etp.ticketservice.domain.model.antivirus.VirusScannable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "antivirus.clamav.enabled", havingValue = "true")
public class AntivirusServiceClamAvImpl extends AbstractAntivirusServiceImpl{

    @Autowired
    private ClamavClient avClient;

    @Override
    protected boolean doPing() {
        try {
            avClient.ping();
        } catch (Exception e) {
            log.error("error.clamav.ping", e);
            return false;
        }
        return true;
    }

    @Override
    protected Map<String, Collection<String>> doScan(VirusScannable scannable) {
        Map<String, Collection<String>> viruses = new HashMap<String, Collection<String>>();
        final ScanResult scanResult = avClient.scan(scannable.getInputStream());
        if (scanResult instanceof ScanResult.VirusFound) {
            viruses = ((ScanResult.VirusFound) scanResult).getFoundViruses();
        }
        return viruses;
    }
}
