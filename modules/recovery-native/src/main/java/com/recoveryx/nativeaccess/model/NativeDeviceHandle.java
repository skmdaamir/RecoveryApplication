package com.recoveryx.nativeaccess.model;

import com.sun.jna.platform.win32.WinNT;

/**
 * Wraps a native Windows device handle with metadata about the opened target.
 *
 * @param handle native handle
 * @param devicePath opened device path
 * @param bytesPerSector sector size associated with the device
 */
public record NativeDeviceHandle(WinNT.HANDLE handle, String devicePath, int bytesPerSector) {
}