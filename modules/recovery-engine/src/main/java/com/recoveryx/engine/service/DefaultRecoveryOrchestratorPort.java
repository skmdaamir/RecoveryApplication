package com.recoveryx.engine.service;

import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.core.domain.recovery.RecoveryRequest;
import com.recoveryx.core.domain.recovery.RecoveryResult;
import com.recoveryx.core.port.recovery.RecoveryOrchestratorPort;
import com.recoveryx.engine.reconstruction.FileReconstructionService;
import com.recoveryx.storage.service.SectorReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of RecoveryOrchestratorPort for executing batch recovery requests.
 */
@Service
public class DefaultRecoveryOrchestratorPort implements RecoveryOrchestratorPort {

    private static final Logger log = LoggerFactory.getLogger(DefaultRecoveryOrchestratorPort.class);

    private final FileReconstructionService reconstructionService;
    private final Map<String, RecoverableFile> scannedFilesRegistry = new ConcurrentHashMap<>();

    public DefaultRecoveryOrchestratorPort(SectorReaderService sectorReaderService) {
        this.reconstructionService = new FileReconstructionService(sectorReaderService);
    }

    /**
     * Registers discovered files from active scan sessions so they can be looked up during recovery.
     */
    public void registerDiscoveredFiles(List<RecoverableFile> files) {
        if (files != null) {
            for (RecoverableFile file : files) {
                scannedFilesRegistry.put(file.fileId(), file);
            }
        }
    }

    @Override
    public RecoveryResult recover(RecoveryRequest request) {
        Objects.requireNonNull(request, "request");
        String targetDir = request.recoveryTarget().targetDirectory();

        int successCount = 0;
        int failCount = 0;
        List<String> outputPaths = new ArrayList<>();

        log.info("Executing recovery request [{}] for {} files to target: {}",
                request.recoveryId(), request.fileIds().size(), targetDir);

        for (String fileId : request.fileIds()) {
            RecoverableFile file = scannedFilesRegistry.get(fileId);
            if (file == null) {
                log.warn("File ID {} not found in scanned registry, skipping", fileId);
                failCount++;
                continue;
            }

            try {
                // Device path can be supplied or inferred
                String devicePath = file.originalPath() != null ? file.originalPath() : "\\\\.\\PhysicalDrive0";
                Path savedPath = reconstructionService.reconstructFile(file, devicePath, targetDir);
                outputPaths.add(savedPath.toString());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to recover file {} (ID: {}): {}", file.name(), fileId, e.getMessage(), e);
                failCount++;
            }
        }

        log.info("Recovery [{}] completed: {} succeeded, {} failed",
                request.recoveryId(), successCount, failCount);

        return new RecoveryResult(request.recoveryId(), successCount, failCount, outputPaths);
    }
}
