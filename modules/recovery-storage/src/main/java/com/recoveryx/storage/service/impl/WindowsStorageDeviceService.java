package com.recoveryx.storage.service.impl;

import com.recoveryx.nativeaccess.service.WindowsNativeDiskAccessService;
import com.recoveryx.storage.model.DeviceGeometry;
import com.recoveryx.storage.model.StorageDevice;
import com.recoveryx.storage.model.StorageDeviceType;
import com.recoveryx.storage.model.StorageVolume;
import com.recoveryx.storage.service.StorageDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Windows storage device service.
 *
 * <p>Enumerates logical volumes via {@link File#listRoots()} as a lightweight
 * cross-platform fallback. Drive geometry details (total bytes, free bytes, label,
 * filesystem type) are obtained directly from the Java NIO {@link java.nio.file.FileStore}
 * API so that no WMI or native calls are required at this stage.</p>
 */
public final class WindowsStorageDeviceService implements StorageDeviceService {

    private static final Logger log = LoggerFactory.getLogger(WindowsStorageDeviceService.class);

    private final WindowsNativeDiskAccessService nativeDiskAccessService;

    public WindowsStorageDeviceService(WindowsNativeDiskAccessService nativeDiskAccessService) {
        this.nativeDiskAccessService = Objects.requireNonNull(
                nativeDiskAccessService,
                "nativeDiskAccessService must not be null");
    }

    /**
     * Lists all logical drives visible to the JVM as {@link StorageDevice} instances.
     * Drive geometry uses {@link File} heuristics; type classification is based on
     * drive letter and size heuristics.
     */
    @Override
    public List<StorageDevice> listDevices() {
        List<StorageDevice> devices = new ArrayList<>();
        File[] roots = File.listRoots();
        if (roots == null) {
            log.warn("File.listRoots() returned null — no drives enumerated.");
            return devices;
        }

        int deviceNumber = 0;
        for (File root : roots) {
            if (!root.exists()) {
                continue;
            }
            try {
                long totalBytes = root.getTotalSpace();
                long freeBytes = root.getFreeSpace();
                String path = root.getAbsolutePath();

                // Classify type: removable heuristic — non-C drives with < 2 TB and readable
                boolean removable = !path.toUpperCase().startsWith("C:") && totalBytes > 0;
                StorageDeviceType type = removable
                        ? StorageDeviceType.USB
                        : StorageDeviceType.HDD;

                DeviceGeometry geometry = new DeviceGeometry(totalBytes > 0 ? totalBytes / 512 : 0, 1, 1, 512, totalBytes);

                StorageDevice device = new StorageDevice(
                        path,
                        buildDisplayName(path, totalBytes, removable),
                        "",
                        "",
                        "",
                        type,
                        removable,
                        geometry,
                        deviceNumber++,
                        0);

                devices.add(device);
                log.debug("Enumerated device: {} ({}, removable={})", path, formatSize(totalBytes), removable);
            } catch (Exception e) {
                log.warn("Failed to enumerate root {}: {}", root, e.getMessage());
            }
        }
        log.info("WindowsStorageDeviceService: enumerated {} drives", devices.size());
        return devices;
    }

    /**
     * Lists all logical volumes including removable drives (USB / card readers).
     */
    @Override
    public List<StorageVolume> listVolumes() {
        List<StorageVolume> volumes = new ArrayList<>();
        File[] roots = File.listRoots();
        if (roots == null) {
            return volumes;
        }

        int deviceNumber = 0;
        for (File root : roots) {
            if (!root.exists()) {
                continue;
            }
            try {
                String path = root.getAbsolutePath();
                long totalBytes = root.getTotalSpace();
                long freeBytes = root.getFreeSpace();
                // FileStore gives us filesystem type (NTFS, FAT32, exFAT, etc.)
                java.nio.file.FileStore store = java.nio.file.Files.getFileStore(root.toPath());
                String fsType = store.type();
                String label = store.name();
                volumes.add(new StorageVolume(path, fsType, label, totalBytes, freeBytes, deviceNumber++));
            } catch (Exception e) {
                log.debug("Could not read volume info for {}: {}", root, e.getMessage());
            }
        }
        return volumes;
    }

    /**
     * Returns only removable volumes (USB drives, SD card readers, etc.).
     * Filters out fixed drives (typically C:\) by checking that total space > 0
     * and the path does not start with "C:".
     */
    public List<StorageVolume> listRemovableVolumes() {
        return listVolumes().stream()
                .filter(v -> !v.volumePath().toUpperCase().startsWith("C:")
                        && v.totalBytes() > 0)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────

    private static String buildDisplayName(String path, long totalBytes, boolean removable) {
        String label = path.replace("\\", "").replace("/", "");
        String type = removable ? "Removable" : "Local Disk";
        return String.format("%s: %s (%s)", label, type, formatSize(totalBytes));
    }

    private static String formatSize(long bytes) {
        if (bytes <= 0) return "Unknown size";
        if (bytes >= 1_073_741_824L) return String.format("%.1f GB", bytes / 1_073_741_824.0);
        if (bytes >= 1_048_576L) return String.format("%.1f MB", bytes / 1_048_576.0);
        return String.format("%.1f KB", bytes / 1_024.0);
    }
}