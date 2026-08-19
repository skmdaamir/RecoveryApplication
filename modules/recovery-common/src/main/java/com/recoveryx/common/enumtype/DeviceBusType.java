package com.recoveryx.common.enumtype;

/**
 * Underlying bus type for a storage device.
 */
public enum DeviceBusType {
    SATA,
    NVME,
    USB,
    MMC,
    SAS,
    VIRTUAL,
    UNKNOWN
}