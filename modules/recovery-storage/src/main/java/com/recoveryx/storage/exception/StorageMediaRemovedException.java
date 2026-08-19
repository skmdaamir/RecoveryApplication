package com.recoveryx.storage.exception;

/**
 * Raised when media becomes unavailable during read operations.
 */
public class StorageMediaRemovedException extends StorageException {

    public StorageMediaRemovedException(String message) {
        super(message);
    }

    public StorageMediaRemovedException(String message, Throwable cause) {
        super(message, cause);
    }
}