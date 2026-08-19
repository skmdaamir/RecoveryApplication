package com.recoveryx.filesystem.ntfs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Decodes the NTFS $FILE_NAME attribute (type 0x30).
 */
public final class NtfsFileNameAttribute {

    private final long parentDirectoryRecordNumber;
    private final Instant creationTime;
    private final Instant modificationTime;
    private final Instant mftModificationTime;
    private final Instant accessTime;
    private final long allocatedSize;
    private final long realSize;
    private final int flags;
    private final byte namespace;
    private final String fileName;

    public NtfsFileNameAttribute(
            long parentDirectoryRecordNumber,
            Instant creationTime,
            Instant modificationTime,
            Instant mftModificationTime,
            Instant accessTime,
            long allocatedSize,
            long realSize,
            int flags,
            byte namespace,
            String fileName) {
        this.parentDirectoryRecordNumber = parentDirectoryRecordNumber;
        this.creationTime = creationTime;
        this.modificationTime = modificationTime;
        this.mftModificationTime = mftModificationTime;
        this.accessTime = accessTime;
        this.allocatedSize = allocatedSize;
        this.realSize = realSize;
        this.flags = flags;
        this.namespace = namespace;
        this.fileName = fileName == null ? "" : fileName;
    }

    public static NtfsFileNameAttribute parse(byte[] attributeData) {
        if (attributeData == null || attributeData.length < 66) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(attributeData).order(ByteOrder.LITTLE_ENDIAN);

        long parentRef = buffer.getLong(0);
        long parentRecordNumber = parentRef & 0x0000FFFFFFFFFFFFL;

        Instant created = fileTimeToInstant(buffer.getLong(8));
        Instant modified = fileTimeToInstant(buffer.getLong(16));
        Instant mftModified = fileTimeToInstant(buffer.getLong(24));
        Instant accessed = fileTimeToInstant(buffer.getLong(32));

        long allocated = buffer.getLong(40);
        long real = buffer.getLong(48);
        int flags = buffer.getInt(56);

        int nameLengthChars = buffer.get(64) & 0xFF;
        byte namespace = buffer.get(65);

        int nameByteLength = nameLengthChars * 2;
        String fileName = "";
        if (66 + nameByteLength <= attributeData.length) {
            fileName = new String(attributeData, 66, nameByteLength, StandardCharsets.UTF_16LE);
        }

        return new NtfsFileNameAttribute(
                parentRecordNumber,
                created,
                modified,
                mftModified,
                accessed,
                allocated,
                real,
                flags,
                namespace,
                fileName);
    }

    public static Instant fileTimeToInstant(long fileTime) {
        if (fileTime <= 0) {
            return Instant.EPOCH;
        }
        long hundredNanos = fileTime % 10_000_000L;
        long epochSeconds = (fileTime / 10_000_000L) - NtfsConstants.FILETIME_EPOCH_DIFF_SECONDS;
        if (epochSeconds < -62135596800L || epochSeconds > 253402300799L) {
            return Instant.EPOCH;
        }
        return Instant.ofEpochSecond(epochSeconds, hundredNanos * 100);
    }

    public long getParentDirectoryRecordNumber() {
        return parentDirectoryRecordNumber;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public Instant getModificationTime() {
        return modificationTime;
    }

    public Instant getMftModificationTime() {
        return mftModificationTime;
    }

    public Instant getAccessTime() {
        return accessTime;
    }

    public long getAllocatedSize() {
        return allocatedSize;
    }

    public long getRealSize() {
        return realSize;
    }

    public int getFlags() {
        return flags;
    }

    public byte getNamespace() {
        return namespace;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isDirectory() {
        return (flags & 0x10000000) != 0 || (flags & 0x00000010) != 0;
    }
}
