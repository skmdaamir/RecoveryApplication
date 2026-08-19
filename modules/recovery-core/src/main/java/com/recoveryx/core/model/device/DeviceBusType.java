package com.recoveryx.core.model.device;

/**
 * Supported storage bus types for device classification.
 */
public enum DeviceBusType {
    UNKNOWN,
    SATA,
    ATA,
    NVME,
    USB,
    SCSI,
    SAS,
    MMC,
    SD,
    VIRTUAL,
    FILE_BACKED
}