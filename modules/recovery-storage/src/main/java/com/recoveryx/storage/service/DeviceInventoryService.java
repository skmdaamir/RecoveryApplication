package com.recoveryx.storage.service;

import com.recoveryx.core.model.device.PhysicalDisk;
import com.recoveryx.core.model.device.StorageVolume;
import com.recoveryx.core.port.storage.DeviceInventoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Application-facing service for retrieving device inventory.
 */
@Service
public class DeviceInventoryService {

    private final DeviceInventoryPort deviceInventoryPort;

    public DeviceInventoryService(DeviceInventoryPort deviceInventoryPort) {
        this.deviceInventoryPort = Objects.requireNonNull(deviceInventoryPort, "deviceInventoryPort must not be null");
    }

    public List<PhysicalDisk> getPhysicalDisks() {
        return deviceInventoryPort.listPhysicalDisks();
    }

    public List<StorageVolume> getVolumes() {
        return deviceInventoryPort.listVolumes();
    }
}