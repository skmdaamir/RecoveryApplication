package com.recoveryx.core.model.device;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable model representing a discoverable storage device.
 */
public final class StorageDevice {

    private final String deviceId;
    private final String displayName;
    private final String devicePath;
    private final DeviceType deviceType;
    private final FileSystemType fileSystemType;
    private final DeviceHealth health;
    private final long totalBytes;
    private final long freeBytes;
    private final boolean removable;
    private final boolean systemDevice;
    private final boolean readable;

    private StorageDevice(Builder builder) {
        this.deviceId = requireNonBlank(builder.deviceId, "deviceId");
        this.displayName = requireNonBlank(builder.displayName, "displayName");
        this.devicePath = requireNonBlank(builder.devicePath, "devicePath");
        this.deviceType = Objects.requireNonNull(builder.deviceType, "deviceType");
        this.fileSystemType = Objects.requireNonNull(builder.fileSystemType, "fileSystemType");
        this.health = Objects.requireNonNull(builder.health, "health");
        this.totalBytes = requireNonNegative(builder.totalBytes, "totalBytes");
        this.freeBytes = requireNonNegative(builder.freeBytes, "freeBytes");
        if (builder.freeBytes > builder.totalBytes) {
            throw new IllegalArgumentException("freeBytes cannot be greater than totalBytes");
        }
        this.removable = builder.removable;
        this.systemDevice = builder.systemDevice;
        this.readable = builder.readable;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDevicePath() {
        return devicePath;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public FileSystemType getFileSystemType() {
        return fileSystemType;
    }

    public DeviceHealth getHealth() {
        return health;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getFreeBytes() {
        return freeBytes;
    }

    public long getUsedBytes() {
        return totalBytes - freeBytes;
    }

    public boolean isRemovable() {
        return removable;
    }

    public boolean isSystemDevice() {
        return systemDevice;
    }

    public boolean isReadable() {
        return readable;
    }

    public Optional<Double> getUsageRatio() {
        if (totalBytes == 0L) {
            return Optional.empty();
        }
        return Optional.of((double) getUsedBytes() / (double) totalBytes);
    }

    public static Builder builder() {
        return new Builder();
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value;
    }

    private static long requireNonNegative(long value, String field) {
        if (value < 0L) {
            throw new IllegalArgumentException(field + " cannot be negative");
        }
        return value;
    }

    public static final class Builder {
        private String deviceId;
        private String displayName;
        private String devicePath;
        private DeviceType deviceType = DeviceType.UNKNOWN;
        private FileSystemType fileSystemType = FileSystemType.UNKNOWN;
        private DeviceHealth health = DeviceHealth.UNKNOWN;
        private long totalBytes;
        private long freeBytes;
        private boolean removable;
        private boolean systemDevice;
        private boolean readable = true;

        private Builder() {
        }

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder devicePath(String devicePath) {
            this.devicePath = devicePath;
            return this;
        }

        public Builder deviceType(DeviceType deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public Builder fileSystemType(FileSystemType fileSystemType) {
            this.fileSystemType = fileSystemType;
            return this;
        }

        public Builder health(DeviceHealth health) {
            this.health = health;
            return this;
        }

        public Builder totalBytes(long totalBytes) {
            this.totalBytes = totalBytes;
            return this;
        }

        public Builder freeBytes(long freeBytes) {
            this.freeBytes = freeBytes;
            return this;
        }

        public Builder removable(boolean removable) {
            this.removable = removable;
            return this;
        }

        public Builder systemDevice(boolean systemDevice) {
            this.systemDevice = systemDevice;
            return this;
        }

        public Builder readable(boolean readable) {
            this.readable = readable;
            return this;
        }

        public StorageDevice build() {
            return new StorageDevice(this);
        }
    }
}