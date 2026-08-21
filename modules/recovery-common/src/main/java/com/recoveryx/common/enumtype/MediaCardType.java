package com.recoveryx.common.enumtype;

/**
 * Identifies the physical type of removable storage media being scanned.
 * Used to apply appropriate sector alignment, cluster size hints, and
 * filesystem format expectations for each card type.
 */
public enum MediaCardType {

    /** Standard SD card (up to 2 GB, FAT16/FAT32) */
    SD("SD Card", 512),

    /** High-Capacity SD card (4 GB – 32 GB, FAT32) */
    SDHC("SDHC Card", 512),

    /** Extended-Capacity SD card (64 GB+, exFAT) */
    SDXC("SDXC Card", 512),

    /** microSD card (adapters present as SD/SDHC/SDXC) */
    MICRO_SD("microSD Card", 512),

    /** CompactFlash card (used in DSLRs, 512-byte sectors) */
    CF("CompactFlash Card", 512),

    /** XQD card (modern DSLRs, 512-byte logical sectors) */
    XQD("XQD Card", 512),

    /** USB flash drive or USB stick */
    USB_DRIVE("USB Flash Drive", 512),

    /** Raw disk image file (.img, .dd, .iso) loaded from filesystem */
    DISK_IMAGE("Disk Image File", 512),

    /** Unknown or unrecognised removable media type */
    UNKNOWN("Unknown Removable Media", 512);

    private final String displayName;
    private final int defaultSectorSize;

    MediaCardType(String displayName, int defaultSectorSize) {
        this.displayName = displayName;
        this.defaultSectorSize = defaultSectorSize;
    }

    /** Human-readable display name shown in the UI. */
    public String displayName() {
        return displayName;
    }

    /** Typical logical sector size for this media type in bytes. */
    public int defaultSectorSize() {
        return defaultSectorSize;
    }

    /**
     * Returns true if the media is a physical card or USB (not a virtual image file).
     */
    public boolean isPhysicalDevice() {
        return this != DISK_IMAGE;
    }

    /**
     * Attempts to infer the media type from a device/file path string.
     *
     * @param path device path or file path (e.g. "E:\", "C:\backup.img", "\\\\.\\PhysicalDrive1")
     * @return best-guess MediaCardType
     */
    public static MediaCardType fromPath(String path) {
        if (path == null) {
            return UNKNOWN;
        }
        String lower = path.toLowerCase();
        if (lower.endsWith(".img") || lower.endsWith(".dd") || lower.endsWith(".iso")
                || lower.endsWith(".raw") || lower.endsWith(".bin")) {
            return DISK_IMAGE;
        }
        return UNKNOWN; // Physical type is resolved by WMI / StorageDeviceService
    }
}
