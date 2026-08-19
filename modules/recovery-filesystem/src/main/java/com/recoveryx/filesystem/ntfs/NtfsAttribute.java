package com.recoveryx.filesystem.ntfs;

/**
 * Base abstract model for an NTFS attribute header.
 */
public abstract class NtfsAttribute {

    private final int attributeType;
    private final int recordLength;
    private final boolean nonResident;
    private final String name;
    private final int flags;
    private final int attributeId;

    protected NtfsAttribute(
            int attributeType,
            int recordLength,
            boolean nonResident,
            String name,
            int flags,
            int attributeId) {
        this.attributeType = attributeType;
        this.recordLength = recordLength;
        this.nonResident = nonResident;
        this.name = name == null ? "" : name;
        this.flags = flags;
        this.attributeId = attributeId;
    }

    public int getAttributeType() {
        return attributeType;
    }

    public int getRecordLength() {
        return recordLength;
    }

    public boolean isNonResident() {
        return nonResident;
    }

    public String getName() {
        return name;
    }

    public int getFlags() {
        return flags;
    }

    public int getAttributeId() {
        return attributeId;
    }
}
