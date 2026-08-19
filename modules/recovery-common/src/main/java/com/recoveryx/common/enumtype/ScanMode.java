package com.recoveryx.common.enumtype;

/**
 * Supported scanning modes.
 */
public enum ScanMode {
    QUICK_SCAN,
    DEEP_SCAN,
    SECTOR_SCAN,
    SIGNATURE_SCAN,
    METADATA_SCAN,
    MFT_SCAN,
    DIRECTORY_SCAN,
    BITMAP_SCAN,
    CLUSTER_SCAN,
    LOST_CLUSTER_SCAN,
    ORPHAN_FILE_SCAN,
    RAW_SECTOR_SCAN
}