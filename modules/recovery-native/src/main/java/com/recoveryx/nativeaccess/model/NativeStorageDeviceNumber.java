package com.recoveryx.nativeaccess.model;

/**
 * Native storage device number mapping returned by Windows for a volume or
 * device.
 *
 * @param deviceType      Windows FILE_DEVICE_* type
 * @param deviceNumber    Windows-assigned device number
 * @param partitionNumber partition number if applicable
 */
public record NativeStorageDeviceNumber(int deviceType,int deviceNumber,int partitionNumber){}