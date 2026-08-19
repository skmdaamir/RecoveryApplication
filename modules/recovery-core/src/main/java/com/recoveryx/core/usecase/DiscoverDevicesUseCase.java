package com.recoveryx.core.usecase;

import com.recoveryx.core.domain.device.DeviceDescriptor;

import java.util.List;

/**
 * Use case for discovering storage devices.
 */
public interface DiscoverDevicesUseCase {

    /**
     * Discovers available devices.
     *
     * @return list of devices
     */
    List<DeviceDescriptor> execute();
}