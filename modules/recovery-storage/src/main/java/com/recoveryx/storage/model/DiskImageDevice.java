package com.recoveryx.storage.model;

import com.recoveryx.common.enumtype.MediaCardType;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a virtual storage device backed by a raw disk image file (.img, .dd, .iso).
 *
 * <p>This model is passed to the scan engine in place of a {@link StorageDevice} when the
 * user chooses to scan a flat binary image file rather than a physical drive.
 * The engine treats it identically — it reads sectors from the image file via
 * {@link com.recoveryx.storage.service.impl.DiskImageSectorReader}.</p>
 */
public final class DiskImageDevice {

    private final Path imagePath;
    private final long sizeBytes;
    private final MediaCardType estimatedCardType;
    private final int sectorSize;

    /**
     * @param imagePath         absolute path to the image file
     * @param sizeBytes         byte length of the image (from file attributes)
     * @param estimatedCardType best-guess media type (use {@link MediaCardType#fromPath} or pass DISK_IMAGE)
     * @param sectorSize        logical sector size; 512 for SD/USB, sometimes 4096 for advanced format
     */
    public DiskImageDevice(Path imagePath,
                           long sizeBytes,
                           MediaCardType estimatedCardType,
                           int sectorSize) {
        this.imagePath = Objects.requireNonNull(imagePath, "imagePath must not be null");
        this.sizeBytes = sizeBytes;
        this.estimatedCardType = estimatedCardType != null ? estimatedCardType : MediaCardType.DISK_IMAGE;
        this.sectorSize = sectorSize > 0 ? sectorSize : 512;
    }

    public Path imagePath() {
        return imagePath;
    }

    public long sizeBytes() {
        return sizeBytes;
    }

    public MediaCardType estimatedCardType() {
        return estimatedCardType;
    }

    public int sectorSize() {
        return sectorSize;
    }

    /** Returns a display-friendly name for the image file. */
    public String displayName() {
        return imagePath.getFileName() + " (" + estimatedCardType.displayName() + ", "
                + formatSize(sizeBytes) + ")";
    }

    private static String formatSize(long bytes) {
        if (bytes >= 1_073_741_824L) {
            return String.format("%.1f GB", bytes / 1_073_741_824.0);
        } else if (bytes >= 1_048_576L) {
            return String.format("%.1f MB", bytes / 1_048_576.0);
        } else {
            return String.format("%.1f KB", bytes / 1_024.0);
        }
    }
}
