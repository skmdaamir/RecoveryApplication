package com.recoveryx.storage.adapter;

import com.recoveryx.core.domain.device.DeviceDescriptor;
import com.recoveryx.core.port.device.DeviceInspectionPort;
import com.recoveryx.nativeaccess.service.WindowsDeviceInspectionService;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Core port adapter for detailed device inspection.
 */
@Component
public class DefaultDeviceInspectionAdapter implements DeviceInspectionPort {

    private final WindowsDeviceInspectionService inspectionService;

    public DefaultDeviceInspectionAdapter(WindowsDeviceInspectionService inspectionService) {
        this.inspectionService = Objects.requireNonNull(inspectionService, "inspectionService must not be null");
    }

    @Override
    public DeviceDescriptor inspectDevice(String deviceId) {
        return inspectionService.inspectDevice(deviceId);
    }
}