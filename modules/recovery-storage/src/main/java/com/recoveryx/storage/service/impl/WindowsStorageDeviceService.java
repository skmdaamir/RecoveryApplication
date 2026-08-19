package com.recoveryx.storage.service.impl;

import com.recoveryx.nativeaccess.service.WindowsNativeDiskAccessService;
import com.recoveryx.storage.model.StorageDevice;
import com.recoveryx.storage.model.StorageVolume;
import com.recoveryx.storage.service.StorageDeviceService;

import java.util.List;
import java.util.Objects;

/**
 * Windows storage device service.
 * Enumeration mapping will be completed once the storage and native model contracts are aligned.
 */
public final class WindowsStorageDeviceService implements StorageDeviceService {

    private final WindowsNativeDiskAccessService nativeDiskAccessService;

    public WindowsStorageDeviceService(WindowsNativeDiskAccessService nativeDiskAccessService) {
        this.nativeDiskAccessService = Objects.requireNonNull(
                nativeDiskAccessService,
                "nativeDiskAccessService must not be null");
    }

    @Override
    public List<StorageDevice> listDevices() {
        return List.of();
    }

    @Override
    public List<StorageVolume> listVolumes() {
        return List.of();
    }
}