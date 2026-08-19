package com.recoveryx.filesystem.ntfs;

/**
 * Constants used in NTFS filesystem structure parsing.
 */
public final class NtfsConstants {

    public static final String MAGIC_FILE = "FILE";
    public static final String MAGIC_BAAD = "BAAD";
    public static final String MAGIC_INDX = "INDX";

    // Attribute Types
    public static final int ATTR_STANDARD_INFORMATION = 0x10;
    public static final int ATTR_ATTRIBUTE_LIST = 0x20;
    public static final int ATTR_FILE_NAME = 0x30;
    public static final int ATTR_OBJECT_ID = 0x40;
    public static final int ATTR_SECURITY_DESCRIPTOR = 0x50;
    public static final int ATTR_VOLUME_NAME = 0x60;
    public static final int ATTR_VOLUME_INFORMATION = 0x70;
    public static final int ATTR_DATA = 0x80;
    public static final int ATTR_INDEX_ROOT = 0x90;
    public static final int ATTR_INDEX_ALLOCATION = 0xA0;
    public static final int ATTR_BITMAP = 0xB0;
    public static final int ATTR_REPARSE_POINT = 0xC0;
    public static final int ATTR_END_MARKER = 0xFFFFFFFF;

    // Record Flags
    public static final short FLAG_IN_USE = 0x0001;
    public static final short FLAG_DIRECTORY = 0x0002;

    // Default Sizes
    public static final int DEFAULT_MFT_RECORD_SIZE = 1024;
    public static final int DEFAULT_SECTOR_SIZE = 512;

    // Windows Filetime conversion (100ns intervals since Jan 1, 1601 to Unix epoch Jan 1, 1970)
    public static final long FILETIME_EPOCH_DIFF_SECONDS = 11644473600L;

    private NtfsConstants() {
    }
}
