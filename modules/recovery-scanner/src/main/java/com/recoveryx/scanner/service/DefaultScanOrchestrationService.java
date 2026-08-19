package com.recoveryx.scanner.service;

import com.recoveryx.common.enumtype.ScanState;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.core.domain.scan.ScanProgress;
import com.recoveryx.core.domain.scan.ScanResultSummary;
import com.recoveryx.core.model.scan.ScanRequest;
import com.recoveryx.core.model.scan.ScanSession;
import com.recoveryx.core.service.ScanOrchestrationService;
import com.recoveryx.filesystem.service.FileSystemService;
import com.recoveryx.scanner.engine.ScanEngine;
import com.recoveryx.scanner.signature.SignatureDatabase;
import com.recoveryx.storage.service.SectorReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Default scan orchestration service.
 * Creates scan sessions and delegates execution to the ScanEngine.
 */
@Service
public class DefaultScanOrchestrationService implements ScanOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(DefaultScanOrchestrationService.class);

    private final ScanEngine scanEngine;

    public DefaultScanOrchestrationService(
            SectorReaderService sectorReaderService,
            FileSystemService fileSystemService) {
        SignatureDatabase db = new SignatureDatabase();
        this.scanEngine = new ScanEngine(sectorReaderService, fileSystemService, db);
    }

    @Override
    public ScanSession createSession(ScanRequest request) {
        Objects.requireNonNull(request, "request");
        if (!request.getStorageDevice().isReadable()) {
            throw new IllegalArgumentException("Selected device is not readable");
        }
        return new ScanSession(UUID.randomUUID().toString(), request, Instant.now(), "CREATED");
    }

    /**
     * Starts a scan and immediately returns a progress snapshot.
     * File discoveries are pushed to the provided consumer on a background thread.
     */
    public ScanProgress startScan(ScanRequest request, Consumer<RecoverableFile> fileConsumer) {
        Objects.requireNonNull(request, "request");
        log.info("Orchestrating scan for device: {}", request.getStorageDevice().getDevicePath());
        return scanEngine.startScan(request, fileConsumer);
    }

    public ScanProgress getProgress(String scanId) {
        return scanEngine.getProgress(scanId);
    }

    public void cancelScan(String scanId) {
        scanEngine.cancelScan(scanId);
    }

    public ScanResultSummary getSummary(String scanId) {
        return scanEngine.getSummary(scanId);
    }
}