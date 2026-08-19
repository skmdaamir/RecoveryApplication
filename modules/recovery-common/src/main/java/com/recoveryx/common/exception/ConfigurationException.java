package com.recoveryx.common.exception;

/**
 * Indicates invalid or inconsistent application configuration.
 */
public class ConfigurationException extends RecoveryXException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}