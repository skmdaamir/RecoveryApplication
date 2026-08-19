package com.recoveryx.storage.exception;

/**
 * Raised when a device cannot be accessed for reading or enumeration.
 */
public class DeviceAccessException extends StorageException {

    public DeviceAccessException(String message) {
        super(message);
    }

    public DeviceAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}