package com.recoveryx.storage.service;

import com.recoveryx.core.model.device.DeviceHealth;
import com.recoveryx.core.model.device.DeviceType;
import com.recoveryx.core.model.device.FileSystemType;
import com.recoveryx.core.model.device.StorageDevice;
import com.recoveryx.core.service.DeviceDiscoveryService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Default device discovery service for the drive selection phase.
 * Enumerates visible filesystem roots and maps them into domain models.
 */
@Service
public class DefaultDeviceDiscoveryService implements DeviceDiscoveryService {

    @Override
    public List<StorageDevice> discoverDevices() {
        File[] roots = File.listRoots();
        if (roots == null || roots.length == 0) {
            return List.of();
        }

        List<StorageDevice> devices = new ArrayList<>();
        for (File root : roots) {
            String path = root.getAbsolutePath();
            boolean readable = safeCanRead(root);
            long total = safeTotal(root);
            long free = safeFree(root);

            devices.add(StorageDevice.builder()
                    .deviceId(normalizeId(path))
                    .displayName(buildDisplayName(path))
                    .devicePath(path)
                    .deviceType(inferType(path))
                    .fileSystemType(FileSystemType.UNKNOWN)
                    .health(readable ? DeviceHealth.HEALTHY : DeviceHealth.INACCESSIBLE)
                    .totalBytes(total)
                    .freeBytes(free)
                    .removable(isRemovable(path))
                    .systemDevice(isSystemDrive(path))
                    .readable(readable)
                    .build());
        }

        devices.sort(Comparator.comparing(StorageDevice::isSystemDevice).reversed()
                .thenComparing(StorageDevice::getDevicePath));
        return List.copyOf(devices);
    }

    private boolean safeCanRead(File root) {
        try {
            return root.canRead();
        } catch (Exception ex) {
            return false;
        }
    }

    private long safeTotal(File root) {
        try {
            return Math.max(0L, root.getTotalSpace());
        } catch (Exception ex) {
            return 0L;
        }
    }

    private long safeFree(File root) {
        try {
            return Math.max(0L, root.getFreeSpace());
        } catch (Exception ex) {
            return 0L;
        }
    }

    private String normalizeId(String path) {
        return path.replace("\\", "").replace(":", "").trim().toUpperCase(Locale.ROOT);
    }

    private String buildDisplayName(String path) {
        return "Drive " + path;
    }

    private boolean isSystemDrive(String path) {
        return path != null && path.toUpperCase(Locale.ROOT).startsWith("C:");
    }

    private boolean isRemovable(String path) {
        String value = path == null ? "" : path.toUpperCase(Locale.ROOT);
        return value.startsWith("E:") || value.startsWith("F:") || value.startsWith("G:") || value.startsWith("H:");
    }

    private DeviceType inferType(String path) {
        if (isRemovable(path)) {
            return DeviceType.USB_FLASH_DRIVE;
        }
        return DeviceType.LOGICAL_VOLUME;
    }
}