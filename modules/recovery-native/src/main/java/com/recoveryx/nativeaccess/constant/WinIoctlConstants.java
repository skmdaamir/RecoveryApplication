package com.recoveryx.nativeaccess.constant;

/**
 * Windows I/O control codes and related constants used for direct storage
 * access.
 */
public final class WinIoctlConstants {

    public static final int IOCTL_DISK_GET_DRIVE_GEOMETRY_EX = 0x000700A0;
    public static final int IOCTL_STORAGE_QUERY_PROPERTY = 0x002D1400;
    public static final int IOCTL_STORAGE_GET_DEVICE_NUMBER = 0x002D1080;

    public static final int STORAGE_DEVICE_PROPERTY = 0;
    public static final int PROPERTY_STANDARD_QUERY = 0;

    private WinIoctlConstants() {
    }
}