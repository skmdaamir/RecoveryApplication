package com.recoveryx.core.port.storage;

import com.recoveryx.core.model.device.PhysicalDisk;
import com.recoveryx.core.model.device.StorageVolume;

import java.util.List;

/**
 * Provides resolved physical disk and logical volume inventory for the
 * application.
 */
public interface DeviceInventoryPort {

    List<PhysicalDisk> listPhysicalDisks();

    List<StorageVolume> listVolumes();
}