package com.recoveryx.storage.model;

import com.recoveryx.nativeaccess.model.NativeDeviceHandle;

import java.util.Objects;

/**
 * Active device read session that owns an opened native handle for sector reads.
 *
 * @param devicePath device path
 * @param bytesPerSector bytes per sector
 * @param handle native handle
 */
public record DeviceReadSession(String devicePath, int bytesPerSector, NativeDeviceHandle handle) {

    public DeviceReadSession {
        Objects.requireNonNull(devicePath, "devicePath must not be null");
        Objects.requireNonNull(handle, "handle must not be null");
        if (bytesPerSector <= 0) {
            throw new IllegalArgumentException("bytesPerSector must be > 0");
        }
    }
}