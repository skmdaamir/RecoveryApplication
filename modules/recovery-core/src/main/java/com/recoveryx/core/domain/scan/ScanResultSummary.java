package com.recoveryx.core.domain.scan;

import com.recoveryx.common.util.ValidationUtils;

/**
 * Summary statistics for a completed or running scan.
 *
 * @param discoveredFiles total discovered files
 * @param deletedFiles deleted files found
 * @param recoverableFiles estimated recoverable files
 * @param duplicateFiles duplicates detected
 * @param totalBytesScanned total bytes scanned
 */
public record ScanResultSummary(
        long discoveredFiles,
        long deletedFiles,
        long recoverableFiles,
        long duplicateFiles,
        long totalBytesScanned) {

    public ScanResultSummary {
        ValidationUtils.requireNonNegative(discoveredFiles, "discoveredFiles");
        ValidationUtils.requireNonNegative(deletedFiles, "deletedFiles");
        ValidationUtils.requireNonNegative(recoverableFiles, "recoverableFiles");
        ValidationUtils.requireNonNegative(duplicateFiles, "duplicateFiles");
        ValidationUtils.requireNonNegative(totalBytesScanned, "totalBytesScanned");
    }
}