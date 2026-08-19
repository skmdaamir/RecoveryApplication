package com.recoveryx.filesystem.ntfs;

import com.recoveryx.core.domain.file.FileFragment;

import java.util.Collections;
import java.util.List;

/**
 * Represents an NTFS non-resident attribute pointing to external disk cluster runs.
 */
public final class NtfsNonResidentAttribute extends NtfsAttribute {

    private final long startingVcn;
    private final long endingVcn;
    private final int dataRunsOffset;
    private final int compressionUnit;
    private final long allocatedSize;
    private final long dataSize;
    private final long initializedSize;
    private final List<FileFragment> fragments;

    public NtfsNonResidentAttribute(
            int attributeType,
            int recordLength,
            String name,
            int flags,
            int attributeId,
            long startingVcn,
            long endingVcn,
            int dataRunsOffset,
            int compressionUnit,
            long allocatedSize,
            long dataSize,
            long initializedSize,
            List<FileFragment> fragments) {
        super(attributeType, recordLength, true, name, flags, attributeId);
        this.startingVcn = startingVcn;
        this.endingVcn = endingVcn;
        this.dataRunsOffset = dataRunsOffset;
        this.compressionUnit = compressionUnit;
        this.allocatedSize = allocatedSize;
        this.dataSize = dataSize;
        this.initializedSize = initializedSize;
        this.fragments = fragments == null ? Collections.emptyList() : Collections.unmodifiableList(fragments);
    }

    public long getStartingVcn() {
        return startingVcn;
    }

    public long getEndingVcn() {
        return endingVcn;
    }

    public int getDataRunsOffset() {
        return dataRunsOffset;
    }

    public int getCompressionUnit() {
        return compressionUnit;
    }

    public long getAllocatedSize() {
        return allocatedSize;
    }

    public long getDataSize() {
        return dataSize;
    }

    public long getInitializedSize() {
        return initializedSize;
    }

    public List<FileFragment> getFragments() {
        return fragments;
    }
}
