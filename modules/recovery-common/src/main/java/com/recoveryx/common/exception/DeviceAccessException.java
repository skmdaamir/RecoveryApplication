package com.recoveryx.common.exception;

/**
 * Indicates a failure while opening or reading a device.
 */
public class DeviceAccessException extends RecoveryXException {

    public DeviceAccessException(String message) {
        super(message);
    }

    public DeviceAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}