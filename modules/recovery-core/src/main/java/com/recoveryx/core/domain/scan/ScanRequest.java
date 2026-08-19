package com.recoveryx.core.domain.scan;

import com.recoveryx.common.enumtype.ScanMode;
import com.recoveryx.common.util.ValidationUtils;

import java.util.Set;

public record ScanRequest(
        String scanId,
        String deviceId,
        String volumeId,
        Set<ScanMode> scanModes,
        boolean includeDeleted,
        boolean includeFormatted,
        boolean includeRawRecovery,
        int threadCount,
        int bufferSizeBytes) {

    public ScanRequest {
        ValidationUtils.requireNotBlank(scanId, "scanId");
        ValidationUtils.requireNotBlank(deviceId, "deviceId");
        ValidationUtils.requireNotEmpty(scanModes, "scanModes");
        ValidationUtils.requirePositive(threadCount, "threadCount");
        ValidationUtils.requirePositive(bufferSizeBytes, "bufferSizeBytes");
        scanModes = Set.copyOf(scanModes);
    }
}