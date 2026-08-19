package com.recoveryx.storage.adapter;

import com.recoveryx.common.exception.DeviceAccessException;
import com.recoveryx.core.model.device.DeviceBusType;
import com.recoveryx.core.model.device.DeviceCategory;
import com.recoveryx.core.model.device.PhysicalDisk;
import com.recoveryx.core.model.device.StorageVolume;
import com.recoveryx.core.model.device.VolumeStatus;
import com.recoveryx.core.port.storage.DeviceInventoryPort;
import com.recoveryx.nativeaccess.model.NativeDeviceHandle;
import com.recoveryx.nativeaccess.model.NativeDriveGeometry;
import com.recoveryx.nativeaccess.model.NativeStorageDeviceDescriptor;
import com.recoveryx.nativeaccess.model.NativeStorageDeviceNumber;
import com.recoveryx.nativeaccess.model.NativeVolumeInfo;
import com.recoveryx.nativeaccess.service.WindowsNativeDiskAccessService;
import com.recoveryx.nativeaccess.service.WindowsVolumeEnumerationService;
import com.recoveryx.nativeaccess.util.WindowsDevicePathFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Default device inventory adapter that resolves Windows physical disks and volumes.
 */
@Component
public class DefaultDeviceInventoryAdapter implements DeviceInventoryPort {

    private static final int MAX_DISKS_TO_PROBE = 64;

    private final WindowsNativeDiskAccessService nativeDiskAccessService;
    private final WindowsVolumeEnumerationService volumeEnumerationService;

    public DefaultDeviceInventoryAdapter(
            WindowsNativeDiskAccessService nativeDiskAccessService,
            WindowsVolumeEnumerationService volumeEnumerationService) {
        this.nativeDiskAccessService = Objects.requireNonNull(nativeDiskAccessService, "nativeDiskAccessService must not be null");
        this.volumeEnumerationService = Objects.requireNonNull(volumeEnumerationService, "volumeEnumerationService must not be null");
    }

    @Override
    public List<PhysicalDisk> listPhysicalDisks() {
        List<PhysicalDisk> disks = new ArrayList<>();

        for (int diskNumber = 0; diskNumber < MAX_DISKS_TO_PROBE; diskNumber++) {
            String devicePath = WindowsDevicePathFactory.physicalDrivePath(diskNumber);

            try {
                NativeDeviceHandle handle = nativeDiskAccessService.openReadOnly(devicePath);
                try {
                    NativeDriveGeometry geometry = nativeDiskAccessService.queryGeometry(handle);
                    NativeStorageDeviceDescriptor descriptor = nativeDiskAccessService.queryStorageDescriptor(handle);

                    DeviceBusType busType = mapBusType(descriptor.busType());
                    DeviceCategory category = classifyDevice(descriptor.removable(), busType, descriptor.product());

                    disks.add(new PhysicalDisk(
                            diskNumber,
                            devicePath,
                            buildDisplayName(diskNumber, descriptor),
                            descriptor.vendor(),
                            descriptor.product(),
                            descriptor.serialNumber(),
                            geometry.totalBytes(),
                            geometry.bytesPerSector(),
                            descriptor.removable(),
                            busType,
                            category
                    ));
                } finally {
                    nativeDiskAccessService.close(handle);
                }
            } catch (DeviceAccessException ex) {
                if (diskNumber == 0 || !devicePath.endsWith(String.valueOf(diskNumber))) {
                    throw ex;
                }
            }
        }

        return List.copyOf(disks);
    }

    @Override
    public List<StorageVolume> listVolumes() {
        List<NativeVolumeInfo> nativeVolumes = volumeEnumerationService.enumerateVolumes();
        List<StorageVolume> volumes = new ArrayList<>();

        for (NativeVolumeInfo nativeVolume : nativeVolumes) {
            Integer diskNumber = null;
            Integer partitionNumber = null;

            try {
                NativeStorageDeviceNumber deviceNumber =
                        nativeDiskAccessService.queryDeviceNumber(nativeVolume.volumeGuidPath());
                diskNumber = deviceNumber.deviceNumber();
                partitionNumber = deviceNumber.partitionNumber();
            } catch (DeviceAccessException ignored) {
                diskNumber = null;
                partitionNumber = null;
            }

            volumes.add(new StorageVolume(
                    nativeVolume.volumeGuidPath(),
                    nativeVolume.deviceName(),
                    nativeVolume.mountPaths(),
                    diskNumber,
                    partitionNumber,
                    nativeVolume.mountPaths().isEmpty() ? VolumeStatus.UNMOUNTED : VolumeStatus.ONLINE
            ));
        }

        return List.copyOf(volumes);
    }

    private String buildDisplayName(int diskNumber, NativeStorageDeviceDescriptor descriptor) {
        String vendor = descriptor.vendor().isBlank() ? "UnknownVendor" : descriptor.vendor();
        String product = descriptor.product().isBlank() ? "UnknownProduct" : descriptor.product();
        return "Disk " + diskNumber + " - " + vendor + " " + product;
    }

    private DeviceBusType mapBusType(int busTypeCode) {
        return switch (busTypeCode) {
            case 3 -> DeviceBusType.ATA;
            case 7 -> DeviceBusType.USB;
            case 8 -> DeviceBusType.SCSI;
            case 10 -> DeviceBusType.SAS;
            case 11 -> DeviceBusType.SATA;
            case 17 -> DeviceBusType.NVME;
            default -> DeviceBusType.UNKNOWN;
        };
    }

    private DeviceCategory classifyDevice(boolean removable, DeviceBusType busType, String productName) {
        String normalizedProduct = productName == null ? "" : productName.toLowerCase(Locale.ROOT);

        if (busType == DeviceBusType.USB && removable) {
            if (normalizedProduct.contains("sd") || normalizedProduct.contains("card")) {
                return DeviceCategory.MEMORY_CARD;
            }
            if (normalizedProduct.contains("flash") || normalizedProduct.contains("thumb") || normalizedProduct.contains("usb")) {
                return DeviceCategory.USB_FLASH_DRIVE;
            }
            return DeviceCategory.USB_FLASH_DRIVE;
        }

        if (busType == DeviceBusType.USB) {
            if (normalizedProduct.contains("ssd")) {
                return DeviceCategory.EXTERNAL_SSD;
            }
            if (normalizedProduct.contains("hdd")) {
                return DeviceCategory.EXTERNAL_HDD;
            }
            return DeviceCategory.EXTERNAL_HDD;
        }

        if (busType == DeviceBusType.NVME) {
            return DeviceCategory.INTERNAL_SSD;
        }

        if (busType == DeviceBusType.SATA || busType == DeviceBusType.ATA || busType == DeviceBusType.SAS || busType == DeviceBusType.SCSI) {
            if (normalizedProduct.contains("ssd")) {
                return DeviceCategory.INTERNAL_SSD;
            }
            return DeviceCategory.INTERNAL_HDD;
        }

        return DeviceCategory.UNKNOWN;
    }
}