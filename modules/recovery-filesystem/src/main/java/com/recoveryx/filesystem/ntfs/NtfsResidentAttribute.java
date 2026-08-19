package com.recoveryx.filesystem.ntfs;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents an NTFS resident attribute whose data is embedded directly in the MFT record.
 */
public final class NtfsResidentAttribute extends NtfsAttribute {

    private final int valueLength;
    private final int valueOffset;
    private final byte[] valueData;

    public NtfsResidentAttribute(
            int attributeType,
            int recordLength,
            String name,
            int flags,
            int attributeId,
            int valueLength,
            int valueOffset,
            byte[] valueData) {
        super(attributeType, recordLength, false, name, flags, attributeId);
        this.valueLength = valueLength;
        this.valueOffset = valueOffset;
        this.valueData = Objects.requireNonNull(valueData, "valueData").clone();
    }

    public int getValueLength() {
        return valueLength;
    }

    public int getValueOffset() {
        return valueOffset;
    }

    public byte[] getValueData() {
        return valueData.clone();
    }
}
