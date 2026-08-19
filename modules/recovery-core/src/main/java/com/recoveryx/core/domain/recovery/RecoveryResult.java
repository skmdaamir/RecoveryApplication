package com.recoveryx.core.domain.recovery;

import com.recoveryx.common.util.ValidationUtils;

import java.util.List;

/**
 * Summary result of a recovery operation.
 *
 * @param recoveryId recovery operation identifier
 * @param recoveredCount successfully recovered file count
 * @param failedCount failed file count
 * @param outputPaths output file paths
 */
public record RecoveryResult(
        String recoveryId,
        int recoveredCount,
        int failedCount,
        List<String> outputPaths) {

    public RecoveryResult {
        ValidationUtils.requireNotBlank(recoveryId, "recoveryId");
        if (recoveredCount < 0) {
            throw new IllegalArgumentException("recoveredCount must be >= 0");
        }
        if (failedCount < 0) {
            throw new IllegalArgumentException("failedCount must be >= 0");
        }
        outputPaths = List.copyOf(outputPaths == null ? List.of() : outputPaths);
    }
}