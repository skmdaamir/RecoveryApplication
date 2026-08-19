package com.recoveryx.filesystem.partition;

import com.recoveryx.common.enumtype.FileSystemType;

/**
 * Common partition types supported by RecoveryX Pro.
 */
public enum PartitionType {
    MBR_FAT16((byte) 0x06, FileSystemType.FAT32, "FAT16"),
    MBR_NTFS((byte) 0x07, FileSystemType.NTFS, "NTFS / exFAT / HPFS"),
    MBR_FAT32((byte) 0x0B, FileSystemType.FAT32, "FAT32 (CHS)"),
    MBR_FAT32_LBA((byte) 0x0C, FileSystemType.FAT32, "FAT32 (LBA)"),
    MBR_EXTENDED((byte) 0x0F, FileSystemType.UNKNOWN, "Extended Partition (LBA)"),
    MBR_LINUX_NATIVE((byte) 0x83, FileSystemType.EXT4, "Linux Native"),
    MBR_GPT_PROTECTIVE((byte) 0xEE, FileSystemType.UNKNOWN, "GPT Protective"),
    GPT_BASIC_DATA((byte) 0x00, FileSystemType.NTFS, "Basic Data Partition"),
    GPT_EFI_SYSTEM((byte) 0x00, FileSystemType.FAT32, "EFI System Partition"),
    GPT_MICROSOFT_RESERVED((byte) 0x00, FileSystemType.UNKNOWN, "Microsoft Reserved Partition"),
    UNKNOWN((byte) 0x00, FileSystemType.UNKNOWN, "Unknown Partition");

    private final byte mbrTypeCode;
    private final FileSystemType defaultFileSystemType;
    private final String description;

    PartitionType(byte mbrTypeCode, FileSystemType defaultFileSystemType, String description) {
        this.mbrTypeCode = mbrTypeCode;
        this.defaultFileSystemType = defaultFileSystemType;
        this.description = description;
    }

    public byte getMbrTypeCode() {
        return mbrTypeCode;
    }

    public FileSystemType getDefaultFileSystemType() {
        return defaultFileSystemType;
    }

    public String getDescription() {
        return description;
    }

    public static PartitionType fromMbrType(byte typeCode) {
        for (PartitionType type : values()) {
            if (type.mbrTypeCode == typeCode && type != GPT_BASIC_DATA && type != GPT_EFI_SYSTEM && type != GPT_MICROSOFT_RESERVED) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
