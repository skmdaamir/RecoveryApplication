package com.recoveryx.core.service;

import com.recoveryx.core.model.device.StorageDevice;

import java.util.List;


public interface DeviceDiscoveryService {
    List<StorageDevice> discoverDevices();
}