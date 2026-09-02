package com.etp.ticketservice.domain.service.antivirus;

import com.etp.ticketservice.domain.exception.AppException;
import com.etp.ticketservice.domain.exception.ServiceUnavailableException;
import com.etp.ticketservice.domain.exception.VirusFoundException;
import com.etp.ticketservice.domain.model.antivirus.*;
import com.etp.ticketservice.domain.model.antivirus.VirusScanResult.VirusScanResultBuilder;
import com.etp.ticketservice.domain.model.antivirus.VirusScanResultFileList.VirusScanResultFileListBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractAntivirusServiceImpl implements AntivirusService{

    @Override
    public boolean ping() {
        try {
            return doPing();
        } catch (Exception e) {
            log.error("error.ping", e);
            return false;
        }
    }

    @Override
    public VirusScanResult scan(Collection<VirusScannable> scannables) {
        VirusScanResultBuilder scanResultBuilder = VirusScanResult.builder();
        boolean virusFound = false;
        try {
            for (VirusScannable scannable : scannables) {
                final VirusScanResult tmptVirusScanResult = scan(scannable);
                virusFound = Result.VIRUS_FOUND.equals(tmptVirusScanResult.getResult());
                tmptVirusScanResult.getCleanFiles().forEach(scanResultBuilder::cleanFile);
                tmptVirusScanResult.getInfectedFiles().forEach(scanResultBuilder::infectedFile);
                tmptVirusScanResult.getErrorFiles().forEach(scanResultBuilder::errorFile);
            }
            scanResultBuilder.result(virusFound ? Result.VIRUS_FOUND : Result.OK);
        } catch (Exception e) {
            scanResultBuilder.result(Result.ERROR);
            log.error("error.antivirus.scan", e);
        }

        VirusScanResult scanResult = scanResultBuilder.build();
        scanResult = processScanResult(scanResult);
        return scanResult;
    }

    @Override
    public VirusScanResult scan(VirusScannable scannable) {
        VirusScanResultBuilder scanResultBuilder = VirusScanResult.builder();
        VirusScanResultFileListBuilder scanFileListBuilder = VirusScanResultFileList.builder()
                .fileName(scannable.getOriginalFilename());

        if (scannable.getSize() <= 0) {
            log.debug("skipping file: {} of size: {}", scannable.getOriginalFilename(), scannable.getSize());
            scanFileListBuilder.result(Result.ERROR);
            scanResultBuilder.errorFile(scanFileListBuilder.build());
            return scanResultBuilder.build();
        }

        log.debug("scanning file: {} of size: {}", scannable.getOriginalFilename(), scannable.getSize());

        checkIfServiceAvailable();
        Map<String, Collection<String>> viruses = doScan(scannable);
        if (viruses.isEmpty()) {
            scanFileListBuilder.result(Result.OK);
            scanResultBuilder.cleanFile(scanFileListBuilder.build());

        } else {
            scanFileListBuilder.fileName(scannable.getOriginalFilename());
            scanFileListBuilder.result(Result.VIRUS_FOUND);
            log.debug("scan result for file: {} viruses: {}", scannable.getOriginalFilename(), viruses);
            for (Map.Entry<String, Collection<String>> virusEntrySet : viruses.entrySet()) {
                scanFileListBuilder.scanFile(VirusScanResultFile.builder().fileAlias(virusEntrySet.getKey())
                        .viruses(virusEntrySet.getValue()).build());
            }
            scanResultBuilder.result(Result.VIRUS_FOUND);
            scanResultBuilder.infectedFile(scanFileListBuilder.build());
        }

        VirusScanResult scanResult = scanResultBuilder.build();
        scanResult = processScanResult(scanResult);
        return scanResult;
    }

    protected void checkIfServiceAvailable() {
        boolean check = ping();
        if (!check) {
            throw new ServiceUnavailableException("error.antivirus.service.unavailable");
        }
    }

    protected abstract boolean doPing();

    protected abstract Map<String, Collection<String>> doScan(VirusScannable scannable);

    protected VirusScanResult processScanResult(VirusScanResult scanResult) throws VirusFoundException {
        List<VirusScanResultFileList> infectedFiles = scanResult.getInfectedFiles();
        if (infectedFiles != null && !infectedFiles.isEmpty()) {
            throw new VirusFoundException("error.antivirus.scan.VIRUS_FOUND");
        }
        List<VirusScanResultFileList> errorFiles = scanResult.getErrorFiles();
        if (errorFiles != null && !errorFiles.isEmpty()) {
            throw new AppException("error.antivirus.scan.ERROR");
        }
        return scanResult;
    }
}
