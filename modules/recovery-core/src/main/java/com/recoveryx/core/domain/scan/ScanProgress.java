package com.recoveryx.core.domain.scan;

import com.recoveryx.common.enumtype.ScanState;
import com.recoveryx.common.util.ValidationUtils;

/**
 * Immutable progress snapshot for a running scan.
 *
 * @param state current scan state
 * @param percentComplete completion percentage from 0 to 100
 * @param scannedBytes processed bytes
 * @param totalBytes total expected bytes
 * @param processedItems processed file or metadata items
 * @param message current progress message
 */
public record ScanProgress(
        ScanState state,
        double percentComplete,
        long scannedBytes,
        long totalBytes,
        long processedItems,
        String message) {

    public ScanProgress {
        ValidationUtils.requireNonNull(state, "state");
        ValidationUtils.requireNonNegative(scannedBytes, "scannedBytes");
        ValidationUtils.requireNonNegative(totalBytes, "totalBytes");
        ValidationUtils.requireNonNegative(processedItems, "processedItems");
        if (percentComplete < 0.0 || percentComplete > 100.0) {
            throw new IllegalArgumentException("percentComplete must be between 0 and 100");
        }
    }
}