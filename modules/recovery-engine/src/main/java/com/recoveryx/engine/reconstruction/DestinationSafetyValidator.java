package com.recoveryx.engine.reconstruction;

import com.recoveryx.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Validates that destination output directories are safe for recovery operations
 * and do not point to the source device or volume being scanned.
 */
public final class DestinationSafetyValidator {

    private static final Logger log = LoggerFactory.getLogger(DestinationSafetyValidator.class);

    private DestinationSafetyValidator() {
    }

    /**
     * Validates that the target directory is safe and accessible.
     *
     * @param targetDirectoryPath path to destination directory
     * @param sourceDevicePath    path to source device or volume (e.g. "\\.\PhysicalDrive1" or "E:\")
     */
    public static void validateSafety(String targetDirectoryPath, String sourceDevicePath) {
        ValidationUtils.requireNotBlank(targetDirectoryPath, "targetDirectoryPath");

        Path target = Paths.get(targetDirectoryPath).toAbsolutePath().normalize();

        // 1. Create directory if it doesn't exist
        try {
            if (!Files.exists(target)) {
                Files.createDirectories(target);
                log.info("Created recovery destination directory: {}", target);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot create destination directory: " + targetDirectoryPath + " - " + e.getMessage());
        }

        if (!Files.isDirectory(target) || !Files.isWritable(target)) {
            throw new IllegalArgumentException("Destination directory is not writable: " + targetDirectoryPath);
        }

        // 2. Prevent recovery back onto the source drive if source is a drive letter (e.g., E:\ or E:)
        if (sourceDevicePath != null && !sourceDevicePath.isBlank()) {
            String cleanSource = sourceDevicePath.toUpperCase().trim();
            if (cleanSource.length() >= 2 && cleanSource.charAt(1) == ':') {
                String sourceDriveLetter = cleanSource.substring(0, 2); // e.g. "E:"
                String targetDriveLetter = target.getRoot() != null ? target.getRoot().toString().toUpperCase().substring(0, 2) : "";

                if (sourceDriveLetter.equals(targetDriveLetter)) {
                    throw new IllegalArgumentException(
                            "DANGER: Cannot recover files back onto the source drive (" + sourceDriveLetter +
                            ")! Choose a destination folder on a different drive to prevent overwriting deleted data.");
                }
            }
        }
    }
}
