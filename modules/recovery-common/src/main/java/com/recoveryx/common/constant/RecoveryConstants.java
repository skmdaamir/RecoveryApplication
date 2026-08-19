package com.recoveryx.common.constant;

/**
 * Global constants used throughout RecoveryX Pro.
 */
public final class RecoveryConstants {

    public static final String APPLICATION_NAME = "RecoveryX Pro";
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

    private RecoveryConstants() {
    }
}