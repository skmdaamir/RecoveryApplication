package com.recoveryx.nativeaccess.service;

import com.recoveryx.common.enumtype.DeviceBusType;
import com.recoveryx.common.enumtype.DeviceType;
import com.recoveryx.common.enumtype.HealthStatus;
import com.recoveryx.core.domain.device.DeviceDescriptor;
import com.recoveryx.core.domain.device.DeviceGeometry;
import com.recoveryx.core.domain.device.DeviceHealthSnapshot;
import com.recoveryx.nativeaccess.model.NativeDeviceHandle;
import com.recoveryx.nativeaccess.model.NativeDriveGeometry;
import com.recoveryx.nativeaccess.model.NativeStorageDeviceDescriptor;
import com.recoveryx.nativeaccess.util.WindowsDevicePathFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Enumerates Windows physical drives by probing standard PhysicalDrive device paths.
 */
@Service
public class WindowsDeviceDiscoveryService {

    private static final int MAX_PROBE_DISKS = 32;

    private final WindowsNativeDiskAccessService diskAccessService;

    public WindowsDeviceDiscoveryService(WindowsNativeDiskAccessService diskAccessService) {
        this.diskAccessService = Objects.requireNonNull(diskAccessService, "diskAccessService must not be null");
    }

    public List<DeviceDescriptor> discoverDevices() {
        List<DeviceDescriptor> devices = new ArrayList<>();

        for (int diskNumber = 0; diskNumber < MAX_PROBE_DISKS; diskNumber++) {
            String path = WindowsDevicePathFactory.physicalDrivePath(diskNumber);
            NativeDeviceHandle handle = null;

            try {
                handle = diskAccessService.openReadOnly(path);
                NativeDriveGeometry geometry = diskAccessService.queryGeometry(handle);
                NativeStorageDeviceDescriptor descriptor = diskAccessService.queryStorageDescriptor(handle);

                devices.add(new DeviceDescriptor(
                        "disk-" + diskNumber,
                        buildDisplayName(diskNumber, descriptor),
                        path,
                        descriptor.serialNumber(),
                        classifyDeviceType(descriptor),
                        mapBusType(descriptor.busType()),
                        descriptor.removable(),
                        diskNumber == 0,
                        new DeviceGeometry(
                                geometry.totalBytes() / Math.max(1, geometry.bytesPerSector()),
                                geometry.bytesPerSector(),
                                geometry.totalBytes()),
                        new DeviceHealthSnapshot(HealthStatus.UNKNOWN, 0, -1, false),
                        List.of()
                ));
            } catch (Exception ignored) {
                // Intentionally ignore inaccessible or nonexistent disk numbers during probe.
            } finally {
                diskAccessService.close(handle);
            }
        }

        return List.copyOf(devices);
    }

    private String buildDisplayName(int diskNumber, NativeStorageDeviceDescriptor descriptor) {
        String vendor = descriptor.vendor().isBlank() ? "UnknownVendor" : descriptor.vendor();
        String product = descriptor.product().isBlank() ? "Disk" : descriptor.product();
        return "Disk %d - %s %s".formatted(diskNumber, vendor, product).trim();
    }

    private DeviceType classifyDeviceType(NativeStorageDeviceDescriptor descriptor) {
        if (descriptor.removable()) {
            return DeviceType.USB_FLASH_DRIVE;
        }

        DeviceBusType busType = mapBusType(descriptor.busType());
        return switch (busType) {
            case USB -> DeviceType.EXTERNAL_HDD;
            case NVME -> DeviceType.SSD;
            case MMC -> DeviceType.MEMORY_CARD;
            case SATA, SAS, UNKNOWN, VIRTUAL -> DeviceType.HDD;
        };
    }

    private DeviceBusType mapBusType(int busTypeCode) {
        return switch (busTypeCode) {
            case 7 -> DeviceBusType.USB;
            case 11 -> DeviceBusType.SATA;
            case 17 -> DeviceBusType.NVME;
            case 12 -> DeviceBusType.SAS;
            case 10 -> DeviceBusType.MMC;
            case 14 -> DeviceBusType.VIRTUAL;
            default -> DeviceBusType.UNKNOWN;
        };
    }
}