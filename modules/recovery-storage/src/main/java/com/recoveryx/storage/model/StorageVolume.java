package com.recoveryx.storage.model;

import java.util.Objects;

/**
 * Logical volume information mapped to a physical device.
 */
public final class StorageVolume {

    private final String volumePath;
    private final String fileSystem;
    private final String label;
    private final long totalBytes;
    private final long freeBytes;
    private final int deviceNumber;

    public StorageVolume(String volumePath,
            String fileSystem,
            String label,
            long totalBytes,
            long freeBytes,
            int deviceNumber) {
        this.volumePath = Objects.requireNonNull(volumePath, "volumePath must not be null");
        this.fileSystem = fileSystem == null ? "" : fileSystem;
        this.label = label == null ? "" : label;
        this.totalBytes = totalBytes;
        this.freeBytes = freeBytes;
        this.deviceNumber = deviceNumber;
    }

    public String volumePath() {
        return volumePath;
    }

    public String fileSystem() {
        return fileSystem;
    }

    public String label() {
        return label;
    }

    public long totalBytes() {
        return totalBytes;
    }

    public long freeBytes() {
        return freeBytes;
    }

    public int deviceNumber() {
        return deviceNumber;
    }
}