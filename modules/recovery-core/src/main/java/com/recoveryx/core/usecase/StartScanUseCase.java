package com.recoveryx.core.usecase;

import com.recoveryx.core.domain.scan.ScanProgress;
import com.recoveryx.core.domain.scan.ScanRequest;

/**
 * Use case for starting a scan.
 */
public interface StartScanUseCase {

    /**
     * Starts a scan operation.
     *
     * @param request scan request
     * @return initial scan progress
     */
    ScanProgress execute(ScanRequest request);
}