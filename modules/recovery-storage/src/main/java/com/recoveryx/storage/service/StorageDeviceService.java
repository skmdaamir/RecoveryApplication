package com.recoveryx.storage.service;

import com.recoveryx.storage.model.StorageDevice;
import com.recoveryx.storage.model.StorageVolume;

import java.util.List;

/**
 * Enumerates physical devices and logical volumes.
 */
public interface StorageDeviceService {

    List<StorageDevice> listDevices();

    List<StorageVolume> listVolumes();
}