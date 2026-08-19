package com.recoveryx.nativeaccess.model;

import com.recoveryx.common.util.ValidationUtils;

/**
 * Immutable request describing a raw device read operation.
 *
 * @param offsetBytes byte offset
 * @param lengthBytes requested byte length
 */
public record RawReadRequest(long offsetBytes, int lengthBytes) {

    public RawReadRequest {
        ValidationUtils.requireNonNegative(offsetBytes, "offsetBytes");
        ValidationUtils.requirePositive(lengthBytes, "lengthBytes");
    }
}