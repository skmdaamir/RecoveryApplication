package com.recoveryx.core.usecase;

/**
 * Use case for stopping a running scan.
 */
public interface StopScanUseCase {

    /**
     * Stops the scan identified by the provided id.
     *
     * @param scanId scan identifier
     */
    void execute(String scanId);
}