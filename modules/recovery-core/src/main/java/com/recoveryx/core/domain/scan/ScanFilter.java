package com.recoveryx.core.domain.scan;

import com.recoveryx.common.enumtype.RecoveryChance;

import java.time.Instant;

/**
 * Filter criteria for scan result searching and UI refinement.
 *
 * @param extension extension filter
 * @param minSize minimum size in bytes
 * @param maxSize maximum size in bytes
 * @param folder folder path prefix
 * @param recoveryChance recovery chance filter
 * @param deletedAfter lower deletion time bound
 * @param deletedBefore upper deletion time bound
 */
public record ScanFilter(
        String extension,
        Long minSize,
        Long maxSize,
        String folder,
        RecoveryChance recoveryChance,
        Instant deletedAfter,
        Instant deletedBefore) {
}