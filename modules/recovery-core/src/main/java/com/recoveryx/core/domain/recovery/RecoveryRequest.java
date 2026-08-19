package com.recoveryx.core.domain.recovery;

import com.recoveryx.common.util.ValidationUtils;

import java.util.Set;

/**
 * Request for recovering one or more files.
 *
 * @param recoveryId recovery operation identifier
 * @param scanId originating scan identifier
 * @param fileIds file identifiers selected for recovery
 * @param recoveryTarget recovery target options
 */
public record RecoveryRequest(
        String recoveryId,
        String scanId,
        Set<String> fileIds,
        RecoveryTarget recoveryTarget) {

    public RecoveryRequest {
        ValidationUtils.requireNotBlank(recoveryId, "recoveryId");
        ValidationUtils.requireNotBlank(scanId, "scanId");
        ValidationUtils.requireNotEmpty(fileIds, "fileIds");
        ValidationUtils.requireNonNull(recoveryTarget, "recoveryTarget");
        fileIds = Set.copyOf(fileIds);
    }
}