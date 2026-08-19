package com.recoveryx.nativeaccess.util;

import com.recoveryx.common.util.ValidationUtils;

/**
 * Builds Windows native device paths for physical drives and volumes.
 */
public final class WindowsDevicePathFactory {

    private WindowsDevicePathFactory() {
    }

    public static String physicalDrivePath(int diskNumber) {
        if (diskNumber < 0) {
            throw new IllegalArgumentException("diskNumber must be >= 0");
        }
        return "\\\\.\\PhysicalDrive" + diskNumber;
    }

    public static String volumePath(String driveLetter) {
        String normalized = ValidationUtils.requireNotBlank(driveLetter, "driveLetter").trim().toUpperCase();
        if (normalized.length() != 1 || normalized.charAt(0) < 'A' || normalized.charAt(0) > 'Z') {
            throw new IllegalArgumentException("driveLetter must be a single alphabetic character");
        }
        return "\\\\.\\" + normalized + ":";
    }
}