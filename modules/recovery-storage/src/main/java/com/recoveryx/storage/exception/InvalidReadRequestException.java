package com.recoveryx.storage.exception;

/**
 * Raised when a sector read request is invalid.
 */
public class InvalidReadRequestException extends StorageException {

    public InvalidReadRequestException(String message) {
        super(message);
    }
}