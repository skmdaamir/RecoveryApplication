package com.recoveryx.ui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.recoveryx.core.service.DeviceDiscoveryService;
import com.recoveryx.core.service.ScanOrchestrationService;
import com.recoveryx.ui.viewmodel.drive.DriveSelectionViewModel;

/**
 * Registers UI phase beans for drive selection.
 */
@Configuration
public class UiPhaseConfiguration {

    @Bean
    public DriveSelectionViewModel driveSelectionViewModel(
            DeviceDiscoveryService deviceDiscoveryService,
            ScanOrchestrationService scanOrchestrationService) {
        return new DriveSelectionViewModel(deviceDiscoveryService, scanOrchestrationService);
    }
}