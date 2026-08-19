package com.recoveryx.core.domain.recovery;

import com.recoveryx.common.util.ValidationUtils;

/**
 * Defines where recovered files should be written.
 *
 * @param targetDirectory destination directory path
 * @param preserveFolderStructure whether original structure should be preserved
 * @param preserveMetadata whether timestamps and permissions should be preserved where supported
 * @param overwriteExisting whether existing files may be overwritten
 */
public record RecoveryTarget(
        String targetDirectory,
        boolean preserveFolderStructure,
        boolean preserveMetadata,
        boolean overwriteExisting) {

    public RecoveryTarget {
        ValidationUtils.requireNotBlank(targetDirectory, "targetDirectory");
    }
}