package com.recoveryx.nativeaccess.util;

import com.recoveryx.common.exception.DeviceAccessException;

/**
 * Converts native Windows error codes into domain exceptions.
 */
public final class NativeErrorUtils {

    private NativeErrorUtils() {
    }

    public static DeviceAccessException deviceAccessFailure(String operation, String path, int errorCode) {
        return new DeviceAccessException(
                "Native device access failed during %s for %s. Windows error code: %d"
                        .formatted(operation, path, errorCode));
    }
}