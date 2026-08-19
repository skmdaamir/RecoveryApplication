package com.recoveryx.core.domain.file;

import com.recoveryx.common.util.ValidationUtils;

/**
 * A contiguous segment of a file on disk.
 *
 * @param startOffsetBytes physical or logical start offset
 * @param lengthBytes fragment length
 * @param sequenceOrder fragment order within file reconstruction
 */
public record FileFragment(long startOffsetBytes, long lengthBytes, int sequenceOrder) {

    public FileFragment {
        ValidationUtils.requireNonNegative(startOffsetBytes, "startOffsetBytes");
        ValidationUtils.requireNonNegative(lengthBytes, "lengthBytes");
        if (sequenceOrder < 0) {
            throw new IllegalArgumentException("sequenceOrder must be >= 0");
        }
    }
}