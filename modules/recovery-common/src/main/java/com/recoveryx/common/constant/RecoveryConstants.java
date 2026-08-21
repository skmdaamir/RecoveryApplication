package com.recoveryx.common.constant;

/**
 * Global constants used throughout RecoveryX Pro.
 */
public final class RecoveryConstants {

    public static final String APPLICATION_NAME = "RecoveryX Pro";
    public static final String APPLICATION_VERSION = "1.0.0";
    public static final String APPLICATION_VENDOR = "RecoveryX";
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String DEFAULT_RECOVERY_DIRECTORY = "RecoveredFiles";
    public static final String DEFAULT_SESSION_DIRECTORY = "sessions";
    public static final String DEFAULT_REPORT_DIRECTORY = "reports";
    public static final int DEFAULT_SCAN_THREADS = 4;
    public static final int MIN_SCAN_THREADS = 1;
    public static final int MAX_SCAN_THREADS = 64;
    public static final int DEFAULT_READ_BUFFER_SIZE = 1024 * 1024;
    public static final int MIN_READ_BUFFER_SIZE = 4 * 1024;
    public static final int MAX_READ_BUFFER_SIZE = 32 * 1024 * 1024;
    public static final int DEFAULT_SECTOR_SIZE = 512;

    // ── Memory card & disk image constants ──────────────────────────────────

    /** Typical logical sector size for SD / SDHC / SDXC / CF / XQD cards (bytes). */
    public static final int SD_CARD_TYPICAL_SECTOR_SIZE = 512;

    /** Typical logical sector size for CompactFlash cards (bytes). */
    public static final int CF_CARD_TYPICAL_SECTOR_SIZE = 512;

    /** File extensions treated as raw disk image files. */
    public static final String[] DISK_IMAGE_EXTENSIONS = {".img", ".dd", ".iso", ".raw", ".bin"};

    /**
     * File format signatures most commonly found on camera memory cards.
     * Used to prioritise carver results when the source is a camera card.
     */
    public static final String[] CAMERA_CARD_PRIORITY_FORMATS = {
        "JPEG", "JPG", "RAW", "CR2", "NEF", "ARW", "DNG", "ORF", "RW2", "PNG", "TIFF", "MP4", "MOV"
    };

    /** Hotplug polling interval in milliseconds (fallback if WMI events unavailable). */
    public static final long HOTPLUG_POLL_INTERVAL_MS = 5_000L;

    private RecoveryConstants() {
    }
}