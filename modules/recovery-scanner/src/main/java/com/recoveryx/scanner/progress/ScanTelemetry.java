package com.recoveryx.scanner.progress;

import com.recoveryx.common.enumtype.ScanState;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.core.domain.scan.ScanProgress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe telemetry tracker for an active scan.
 * Tracks bytes scanned, speed (MB/s), discovered file counts and emits ScanProgress snapshots.
 */
public final class ScanTelemetry {

    private final String scanId;
    private final long totalBytes;
    private final long startTimeMs;

    private final AtomicLong scannedBytes = new AtomicLong(0);
    private final AtomicLong discoveredFiles = new AtomicLong(0);
    private final AtomicReference<ScanState> state = new AtomicReference<>(ScanState.RUNNING);
    private final List<RecoverableFile> discoveredFileList = new CopyOnWriteArrayList<>();

    public ScanTelemetry(String scanId, long totalBytes) {
        this.scanId = scanId;
        this.totalBytes = totalBytes;
        this.startTimeMs = System.currentTimeMillis();
    }

    public void addScannedBytes(long bytes) {
        scannedBytes.addAndGet(bytes);
    }

    public void recordFile(RecoverableFile file) {
        discoveredFiles.incrementAndGet();
        if (file != null) {
            discoveredFileList.add(file);
        }
    }

    public void setState(ScanState newState) {
        state.set(newState);
    }

    public ScanState getState() {
        return state.get();
    }

    /**
     * Returns an immutable progress snapshot at this moment.
     */
    public ScanProgress snapshot() {
        long scanned = scannedBytes.get();
        long total = Math.max(1, totalBytes);
        double percent = Math.min(100.0, (scanned * 100.0) / total);

        long elapsedMs = Math.max(1, System.currentTimeMillis() - startTimeMs);
        double mbPerSec = (scanned / 1024.0 / 1024.0) / (elapsedMs / 1000.0);

        long remainingBytes = Math.max(0, total - scanned);
        long etaSeconds = mbPerSec > 0 ? (long) (remainingBytes / 1024.0 / 1024.0 / mbPerSec) : 0;

        String message = String.format(
                "Scanning at %.1f MB/s | Found %d files | ETA: %ds",
                mbPerSec, discoveredFiles.get(), etaSeconds);

        return new ScanProgress(
                state.get(),
                percent,
                scanned,
                totalBytes,
                discoveredFiles.get(),
                message);
    }

    public String getScanId() {
        return scanId;
    }

    public long getDiscoveredFileCount() {
        return discoveredFiles.get();
    }

    public List<RecoverableFile> getDiscoveredFiles() {
        return Collections.unmodifiableList(discoveredFileList);
    }
}
