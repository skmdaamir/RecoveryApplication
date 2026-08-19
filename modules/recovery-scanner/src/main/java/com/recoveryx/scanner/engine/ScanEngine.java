package com.recoveryx.scanner.engine;

import com.recoveryx.core.model.scan.ScanMode;
import com.recoveryx.common.enumtype.ScanState;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.core.domain.scan.ScanProgress;
import com.recoveryx.core.domain.scan.ScanResultSummary;
import com.recoveryx.core.model.scan.ScanRequest;
import com.recoveryx.filesystem.service.FileSystemService;
import com.recoveryx.scanner.carver.FileCarver;
import com.recoveryx.scanner.progress.ScanTelemetry;
import com.recoveryx.scanner.signature.SignatureDatabase;
import com.recoveryx.storage.model.SectorReadResult;
import com.recoveryx.storage.service.SectorReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Dual-mode scan engine that supports Quick Scan (filesystem-based) and
 * Deep Scan (raw sector carving) for maximum file recovery.
 */
public final class ScanEngine {

    private static final Logger log = LoggerFactory.getLogger(ScanEngine.class);

    private static final int BYTES_PER_SECTOR = 512;
    private static final int CHUNK_SECTORS = 2048; // 1 MB chunks

    private final SectorReaderService sectorReaderService;
    private final FileSystemService fileSystemService;
    private final SignatureDatabase signatureDatabase;

    private final ConcurrentHashMap<String, ScanTelemetry> activeTelemetry = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicBoolean> cancelFlags = new ConcurrentHashMap<>();

    public ScanEngine(
            SectorReaderService sectorReaderService,
            FileSystemService fileSystemService,
            SignatureDatabase signatureDatabase) {
        this.sectorReaderService = Objects.requireNonNull(sectorReaderService, "sectorReaderService");
        this.fileSystemService = Objects.requireNonNull(fileSystemService, "fileSystemService");
        this.signatureDatabase = Objects.requireNonNull(signatureDatabase, "signatureDatabase");
    }

    /**
     * Starts a scan based on the request. Runs asynchronously using a virtual thread.
     *
     * @param request       the scan request containing device, mode, and options
     * @param fileConsumer  consumer called for every discovered file (can be null)
     * @return initial ScanProgress snapshot
     */
    public ScanProgress startScan(ScanRequest request, Consumer<RecoverableFile> fileConsumer) {
        Objects.requireNonNull(request, "request");
        String scanId = request.getRequestId();
        String devicePath = request.getStorageDevice().getDevicePath();
        long totalBytes = request.getStorageDevice().getTotalBytes();

        ScanTelemetry telemetry = new ScanTelemetry(scanId, totalBytes);
        activeTelemetry.put(scanId, telemetry);
        AtomicBoolean cancel = new AtomicBoolean(false);
        cancelFlags.put(scanId, cancel);

        log.info("Starting scan [{}] on {} | Mode: {}", scanId, devicePath, request.getScanMode());

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        executor.submit(() -> runScan(request, telemetry, cancel, fileConsumer));
        executor.shutdown();

        return telemetry.snapshot();
    }

    /**
     * Returns the current scan progress for a given scan ID.
     */
    public ScanProgress getProgress(String scanId) {
        ScanTelemetry telemetry = activeTelemetry.get(scanId);
        if (telemetry == null) {
            return new ScanProgress(ScanState.COMPLETED, 100.0, 0, 0, 0, "Scan not found or completed");
        }
        return telemetry.snapshot();
    }

    /**
     * Cancels an active scan.
     */
    public void cancelScan(String scanId) {
        AtomicBoolean flag = cancelFlags.get(scanId);
        if (flag != null) {
            flag.set(true);
        }
        ScanTelemetry telemetry = activeTelemetry.get(scanId);
        if (telemetry != null) {
            telemetry.setState(ScanState.CANCELLED);
        }
        log.info("Scan [{}] cancellation requested", scanId);
    }

    /**
     * Returns scan result summary for a completed or running scan.
     */
    public ScanResultSummary getSummary(String scanId) {
        ScanTelemetry telemetry = activeTelemetry.get(scanId);
        if (telemetry == null) {
            return new ScanResultSummary(0, 0, 0, 0, 0);
        }
        long total = telemetry.getDiscoveredFileCount();
        return new ScanResultSummary(total, total, total, 0, telemetry.snapshot().scannedBytes());
    }

    // ==================== Private Scan Logic ====================

    private void runScan(
            ScanRequest request,
            ScanTelemetry telemetry,
            AtomicBoolean cancel,
            Consumer<RecoverableFile> fileConsumer) {

        String devicePath = request.getStorageDevice().getDevicePath();

        try {
            ScanMode mode = request.getScanMode();

            if (mode == ScanMode.QUICK) {
                runQuickScan(devicePath, request, telemetry, fileConsumer);
            } else if (mode == ScanMode.DEEP || mode == ScanMode.SIGNATURE
                    || mode == ScanMode.SECTOR) {
                runDeepScan(devicePath, request, telemetry, cancel, fileConsumer);
            } else {
                // Default: quick scan first, then deep if not cancelled
                runQuickScan(devicePath, request, telemetry, fileConsumer);
                if (!cancel.get()) {
                    runDeepScan(devicePath, request, telemetry, cancel, fileConsumer);
                }
            }

            if (!cancel.get()) {
                telemetry.setState(ScanState.COMPLETED);
                log.info("Scan [{}] completed. Found {} files.", request.getRequestId(), telemetry.getDiscoveredFileCount());
            }
        } catch (Exception e) {
            log.error("Scan [{}] failed: {}", request.getRequestId(), e.getMessage(), e);
            telemetry.setState(ScanState.FAILED);
        }
    }

    private void runQuickScan(
            String devicePath,
            ScanRequest request,
            ScanTelemetry telemetry,
            Consumer<RecoverableFile> fileConsumer) {

        log.info("Quick scan starting on {}", devicePath);
        long totalBytes = request.getStorageDevice().getTotalBytes();
        long totalSectors = totalBytes / BYTES_PER_SECTOR;

        try {
            List<RecoverableFile> files = fileSystemService.scanFilesystem(
                    devicePath,
                    0L,
                    totalSectors,
                    BYTES_PER_SECTOR,
                    file -> {
                        telemetry.recordFile(file);
                        if (fileConsumer != null) {
                            fileConsumer.accept(file);
                        }
                    });

            // Treat quick scan as consuming 20% of total work
            telemetry.addScannedBytes(totalBytes / 5);
            log.info("Quick scan found {} files on {}", files.size(), devicePath);
        } catch (Exception e) {
            log.warn("Quick scan error on {}: {}", devicePath, e.getMessage());
        }
    }

    private void runDeepScan(
            String devicePath,
            ScanRequest request,
            ScanTelemetry telemetry,
            AtomicBoolean cancel,
            Consumer<RecoverableFile> fileConsumer) {

        long totalBytes = request.getStorageDevice().getTotalBytes();
        long totalSectors = totalBytes / BYTES_PER_SECTOR;
        FileCarver carver = new FileCarver(signatureDatabase);

        log.info("Deep scan starting on {} ({} sectors total)", devicePath, totalSectors);

        for (long sector = 0; sector < totalSectors && !cancel.get(); sector += CHUNK_SECTORS) {
            int sectorsToRead = (int) Math.min(CHUNK_SECTORS, totalSectors - sector);

            try {
                SectorReadResult result = sectorReaderService.read(
                        devicePath, sector, sectorsToRead, BYTES_PER_SECTOR);

                byte[] data = result.data();
                long chunkStartByte = sector * BYTES_PER_SECTOR;

                List<RecoverableFile> carved = carver.carve(data, chunkStartByte);
                for (RecoverableFile file : carved) {
                    telemetry.recordFile(file);
                    if (fileConsumer != null) {
                        fileConsumer.accept(file);
                    }
                }

                telemetry.addScannedBytes((long) sectorsToRead * BYTES_PER_SECTOR);

            } catch (Exception e) {
                // Skip bad sectors and continue
                log.debug("Sector read error at sector {}: {}", sector, e.getMessage());
                telemetry.addScannedBytes((long) sectorsToRead * BYTES_PER_SECTOR);
            }
        }
    }
}
