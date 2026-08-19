package com.recoveryx.filesystem.ntfs;

import com.recoveryx.common.enumtype.FileSystemType;
import com.recoveryx.common.util.ValidationUtils;
import com.recoveryx.filesystem.common.BootSectorInfo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Parses and represents the NTFS Boot Sector (LBA 0 of partition).
 */
public final class NtfsBootSector {

    private final int bytesPerSector;
    private final int sectorsPerCluster;
    private final long totalSectors;
    private final long mftClusterLba;
    private final long mftMirrClusterLba;
    private final int mftRecordSizeBytes;
    private final long volumeSerialNumber;

    private NtfsBootSector(
            int bytesPerSector,
            int sectorsPerCluster,
            long totalSectors,
            long mftClusterLba,
            long mftMirrClusterLba,
            int mftRecordSizeBytes,
            long volumeSerialNumber) {
        this.bytesPerSector = bytesPerSector;
        this.sectorsPerCluster = sectorsPerCluster;
        this.totalSectors = totalSectors;
        this.mftClusterLba = mftClusterLba;
        this.mftMirrClusterLba = mftMirrClusterLba;
        this.mftRecordSizeBytes = mftRecordSizeBytes;
        this.volumeSerialNumber = volumeSerialNumber;
    }

    public static NtfsBootSector parse(byte[] sector0) {
        ValidationUtils.requireNonNull(sector0, "sector0");
        if (sector0.length < 512) {
            throw new IllegalArgumentException("Boot sector must be at least 512 bytes");
        }

        ByteBuffer buffer = ByteBuffer.wrap(sector0).order(ByteOrder.LITTLE_ENDIAN);

        String oemId = new String(sector0, 3, 8, StandardCharsets.US_ASCII);
        if (!"NTFS    ".equals(oemId)) {
            throw new IllegalArgumentException("Invalid NTFS OEM ID: " + oemId);
        }

        int bytesPerSector = buffer.getShort(0x0B) & 0xFFFF;
        if (bytesPerSector <= 0) {
            bytesPerSector = 512;
        }

        int sectorsPerCluster = buffer.get(0x0D) & 0xFF;
        if (sectorsPerCluster <= 0) {
            sectorsPerCluster = 8;
        }

        long totalSectors = buffer.getLong(0x28);
        long mftCluster = buffer.getLong(0x30);
        long mftMirrCluster = buffer.getLong(0x38);

        byte rawClustersPerMft = buffer.get(0x40);
        int mftRecordSize;
        if (rawClustersPerMft > 0) {
            mftRecordSize = rawClustersPerMft * sectorsPerCluster * bytesPerSector;
        } else {
            // Negative exponent value (2 ^ -value) e.g., -10 -> 2^10 = 1024 bytes
            mftRecordSize = 1 << (-rawClustersPerMft);
        }

        long serial = buffer.getLong(0x48);

        return new NtfsBootSector(
                bytesPerSector,
                sectorsPerCluster,
                totalSectors,
                mftCluster,
                mftMirrCluster,
                mftRecordSize > 0 ? mftRecordSize : 1024,
                serial);
    }

    public int getBytesPerSector() {
        return bytesPerSector;
    }

    public int getSectorsPerCluster() {
        return sectorsPerCluster;
    }

    public int getClusterSizeBytes() {
        return bytesPerSector * sectorsPerCluster;
    }

    public long getTotalSectors() {
        return totalSectors;
    }

    public long getMftClusterLba() {
        return mftClusterLba;
    }

    public long getMftOffsetBytes() {
        return mftClusterLba * getClusterSizeBytes();
    }

    public long getMftMirrClusterLba() {
        return mftMirrClusterLba;
    }

    public int getMftRecordSizeBytes() {
        return mftRecordSizeBytes;
    }

    public long getVolumeSerialNumber() {
        return volumeSerialNumber;
    }

    public String getVolumeSerialNumberHex() {
        return String.format("%016X", volumeSerialNumber);
    }

    public BootSectorInfo toBootSectorInfo() {
        return new BootSectorInfo(
                FileSystemType.NTFS,
                bytesPerSector,
                sectorsPerCluster,
                totalSectors,
                "NTFS Volume",
                getVolumeSerialNumberHex());
    }
}
