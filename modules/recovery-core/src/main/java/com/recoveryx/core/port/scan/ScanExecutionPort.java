package com.recoveryx.core.port.scan;

import com.recoveryx.core.domain.scan.ScanProgress;
import com.recoveryx.core.domain.scan.ScanRequest;
import com.recoveryx.core.domain.scan.ScanResultSummary;

/**
 * Executes scan operations over devices and volumes.
 */
public interface ScanExecutionPort {

    /**
     * Starts a scan.
     *
     * @param request scan request
     * @return initial scan progress
     */
    ScanProgress startScan(ScanRequest request);

    /**
     * Pauses a running scan.
     *
     * @param scanId scan identifier
     */
    void pauseScan(String scanId);

    /**
     * Resumes a paused scan.
     *
     * @param scanId scan identifier
     */
    void resumeScan(String scanId);

    /**
     * Stops a running scan.
     *
     * @param scanId scan identifier
     */
    void stopScan(String scanId);

    /**
     * Returns latest progress for a scan.
     *
     * @param scanId scan identifier
     * @return current progress snapshot
     */
    ScanProgress progress(String scanId);

    /**
     * Returns summary after or during a scan.
     *
     * @param scanId scan identifier
     * @return scan result summary
     */
    ScanResultSummary summary(String scanId);
}