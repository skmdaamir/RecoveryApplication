package com.recoveryx.storage.adapter;

import com.recoveryx.core.domain.device.DeviceDescriptor;
import com.recoveryx.core.port.device.DeviceDiscoveryPort;
import com.recoveryx.nativeaccess.service.WindowsDeviceDiscoveryService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Core port adapter for native Windows device discovery.
 */
@Component
public class DefaultDeviceDiscoveryAdapter implements DeviceDiscoveryPort {

    private final WindowsDeviceDiscoveryService discoveryService;

    public DefaultDeviceDiscoveryAdapter(WindowsDeviceDiscoveryService discoveryService) {
        this.discoveryService = Objects.requireNonNull(discoveryService, "discoveryService must not be null");
    }

    @Override
    public List<DeviceDescriptor> discoverDevices() {
        return discoveryService.discoverDevices();
    }
}