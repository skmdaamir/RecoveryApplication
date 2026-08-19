package com.recoveryx.scanner.progress;

import com.recoveryx.common.enumtype.ScanState;
import com.recoveryx.core.domain.scan.ScanProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScanTelemetryTest {

    @Test
    void shouldStartAtZeroProgress() {
        ScanTelemetry telemetry = new ScanTelemetry("scan-001", 1_000_000L);
        ScanProgress snap = telemetry.snapshot();

        assertEquals(ScanState.RUNNING, snap.state());
        assertEquals(0.0, snap.percentComplete(), 0.01);
        assertEquals(0L, snap.scannedBytes());
        assertEquals(1_000_000L, snap.totalBytes());
        assertEquals(0L, snap.processedItems());
    }

    @Test
    void shouldUpdateProgressAfterScanningBytes() {
        ScanTelemetry telemetry = new ScanTelemetry("scan-002", 1_000_000L);
        telemetry.addScannedBytes(500_000L);

        ScanProgress snap = telemetry.snapshot();
        assertEquals(50.0, snap.percentComplete(), 1.0);
        assertEquals(500_000L, snap.scannedBytes());
    }

    @Test
    void shouldRecordFilesDiscovered() {
        ScanTelemetry telemetry = new ScanTelemetry("scan-003", 1_000_000L);

        for (int i = 0; i < 5; i++) {
            telemetry.recordFile(null); // simulate recording files (null allowed in test)
        }

        assertEquals(5L, telemetry.getDiscoveredFileCount());
        ScanProgress snap = telemetry.snapshot();
        assertEquals(5L, snap.processedItems());
    }

    @Test
    void shouldTransitionStateCorrectly() {
        ScanTelemetry telemetry = new ScanTelemetry("scan-004", 1_000_000L);

        assertEquals(ScanState.RUNNING, telemetry.getState());

        telemetry.setState(ScanState.COMPLETED);
        assertEquals(ScanState.COMPLETED, telemetry.getState());
        assertEquals(ScanState.COMPLETED, telemetry.snapshot().state());
    }

    @Test
    void shouldNotExceed100PercentComplete() {
        ScanTelemetry telemetry = new ScanTelemetry("scan-005", 1_000_000L);
        telemetry.addScannedBytes(2_000_000L); // More than total

        ScanProgress snap = telemetry.snapshot();
        assertTrue(snap.percentComplete() <= 100.0, "Percent should never exceed 100");
    }
}
