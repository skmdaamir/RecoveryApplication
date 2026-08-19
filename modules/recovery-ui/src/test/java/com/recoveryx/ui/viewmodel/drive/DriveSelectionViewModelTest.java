package com.recoveryx.ui.viewmodel.drive;

import com.recoveryx.core.model.device.DeviceHealth;
import com.recoveryx.core.model.device.DeviceType;
import com.recoveryx.core.model.device.FileSystemType;
import com.recoveryx.core.model.device.StorageDevice;
import com.recoveryx.core.model.scan.ScanSession;
import com.recoveryx.core.service.DeviceDiscoveryService;
import com.recoveryx.core.service.ScanOrchestrationService;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DriveSelectionViewModelTest {

    @Test
    void shouldCreateQuickScanSessionForSelectedDevice() {
        StorageDevice device = StorageDevice.builder()
                .deviceId("D")
                .displayName("Drive D:\\")
                .devicePath("D:\\")
                .deviceType(DeviceType.LOGICAL_VOLUME)
                .fileSystemType(FileSystemType.NTFS)
                .health(DeviceHealth.HEALTHY)
                .totalBytes(2_000_000L)
                .freeBytes(1_000_000L)
                .readable(true)
                .build();

        DeviceDiscoveryService discoveryService = mock(DeviceDiscoveryService.class);
        ScanOrchestrationService orchestrationService = mock(ScanOrchestrationService.class);

        when(orchestrationService.createSession(any())).thenAnswer(invocation ->
                new ScanSession("session-1", invocation.getArgument(0), Instant.now(), "CREATED"));

        DriveSelectionViewModel viewModel = new DriveSelectionViewModel(discoveryService, orchestrationService);
        viewModel.setSelectedDevice(new DeviceItemViewModel(device));

        ScanSession session = viewModel.startQuickScan();

        assertNotNull(session);
        assertEquals("session-1", session.getSessionId());
        verify(orchestrationService, times(1)).createSession(any());
    }

    @Test
    void shouldRejectQuickScanWithoutSelection() {
        DeviceDiscoveryService discoveryService = mock(DeviceDiscoveryService.class);
        ScanOrchestrationService orchestrationService = mock(ScanOrchestrationService.class);

        DriveSelectionViewModel viewModel = new DriveSelectionViewModel(discoveryService, orchestrationService);

        assertThrows(IllegalStateException.class, viewModel::startQuickScan);
    }
}