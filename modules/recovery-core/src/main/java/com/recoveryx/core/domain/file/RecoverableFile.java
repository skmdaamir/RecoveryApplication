package com.recoveryx.core.domain.file;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.common.enumtype.HealthStatus;
import com.recoveryx.common.enumtype.RecoveryChance;
import com.recoveryx.common.util.CollectionUtils;
import com.recoveryx.common.util.ValidationUtils;

import java.time.Instant;
import java.util.List;

/**
 * Canonical representation of a recoverable file candidate.
 *
 * @param fileId unique file identifier
 * @param name file name
 * @param extension file extension
 * @param category file category
 * @param originalPath original path if known
 * @param currentPath current recovered or scanned path if known
 * @param fileSize file size in bytes
 * @param deletedDate deletion timestamp if available
 * @param createdDate creation timestamp if available
 * @param modifiedDate modification timestamp if available
 * @param healthStatus file integrity health
 * @param recoveryChance estimated recovery chance
 * @param previewAvailable whether preview is available
 * @param duplicate whether duplicate candidate detected
 * @param fragments reconstruction fragments
 * @param signatureMatch detected signature info if any
 */
public record RecoverableFile(
        String fileId,
        String name,
        String extension,
        FileCategory category,
        String originalPath,
        String currentPath,
        long fileSize,
        Instant deletedDate,
        Instant createdDate,
        Instant modifiedDate,
        HealthStatus healthStatus,
        RecoveryChance recoveryChance,
        boolean previewAvailable,
        boolean duplicate,
        List<FileFragment> fragments,
        FileSignatureMatch signatureMatch) {

    public RecoverableFile {
        ValidationUtils.requireNotBlank(fileId, "fileId");
        ValidationUtils.requireNotBlank(name, "name");
        ValidationUtils.requireNotBlank(extension, "extension");
        ValidationUtils.requireNonNull(category, "category");
        ValidationUtils.requireNonNegative(fileSize, "fileSize");
        ValidationUtils.requireNonNull(healthStatus, "healthStatus");
        ValidationUtils.requireNonNull(recoveryChance, "recoveryChance");
        fragments = CollectionUtils.immutableCopy(fragments == null ? List.of() : fragments);
    }
}