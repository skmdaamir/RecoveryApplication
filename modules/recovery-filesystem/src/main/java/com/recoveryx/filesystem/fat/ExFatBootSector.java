package com.recoveryx.filesystem.fat;

import com.recoveryx.common.enumtype.FileSystemType;
import com.recoveryx.common.util.ValidationUtils;
import com.recoveryx.filesystem.common.BootSectorInfo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Parses and models the exFAT Boot Sector and Main Boot Region.
 */
public final class ExFatBootSector {

    private final int bytesPerSector;
    private final int sectorsPerCluster;
    private final long fatOffsetSector;
    private final long fatLengthSectors;
    private final long clusterHeapOffsetSector;
    private final long clusterCount;
    private final long rootDirectoryCluster;
    private final long volumeSerialNumber;
    private final long volumeLengthSectors;

    private ExFatBootSector(
            int bytesPerSector,
            int sectorsPerCluster,
            long fatOffsetSector,
            long fatLengthSectors,
            long clusterHeapOffsetSector,
            long clusterCount,
            long rootDirectoryCluster,
            long volumeSerialNumber,
            long volumeLengthSectors) {
        this.bytesPerSector = bytesPerSector;
        this.sectorsPerCluster = sectorsPerCluster;
        this.fatOffsetSector = fatOffsetSector;
        this.fatLengthSectors = fatLengthSectors;
        this.clusterHeapOffsetSector = clusterHeapOffsetSector;
        this.clusterCount = clusterCount;
        this.rootDirectoryCluster = rootDirectoryCluster;
        this.volumeSerialNumber = volumeSerialNumber;
        this.volumeLengthSectors = volumeLengthSectors;
    }

    public static ExFatBootSector parse(byte[] sector0) {
        ValidationUtils.requireNonNull(sector0, "sector0");
        if (sector0.length < 512) {
            throw new IllegalArgumentException("exFAT boot sector must be at least 512 bytes");
        }

        String oemId = new String(sector0, 3, 8, StandardCharsets.US_ASCII);
        if (!"EXFAT   ".equals(oemId)) {
            throw new IllegalArgumentException("Invalid exFAT OEM ID: " + oemId);
        }

        ByteBuffer buffer = ByteBuffer.wrap(sector0).order(ByteOrder.LITTLE_ENDIAN);

        long volumeLength = buffer.getLong(0x48);
        long fatOffset = Integer.toUnsignedLong(buffer.getInt(0x50));
        long fatLength = Integer.toUnsignedLong(buffer.getInt(0x54));
        long clusterHeapOffset = Integer.toUnsignedLong(buffer.getInt(0x58));
        long clusterCount = Integer.toUnsignedLong(buffer.getInt(0x5C));
        long rootCluster = Integer.toUnsignedLong(buffer.getInt(0x60));
        long serialNumber = Integer.toUnsignedLong(buffer.getInt(0x64));

        byte bytesPerSectorShift = buffer.get(0x6C);
        byte sectorsPerClusterShift = buffer.get(0x6D);

        int bytesPerSector = (bytesPerSectorShift >= 9 && bytesPerSectorShift <= 12) ? (1 << bytesPerSectorShift) : 512;
        int sectorsPerCluster = (sectorsPerClusterShift >= 0 && sectorsPerClusterShift <= 25) ? (1 << sectorsPerClusterShift) : 8;

        return new ExFatBootSector(
                bytesPerSector,
                sectorsPerCluster,
                fatOffset,
                fatLength,
                clusterHeapOffset,
                clusterCount,
                rootCluster > 0 ? rootCluster : 2,
                serialNumber,
                volumeLength);
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

    public long getFatOffsetSector() {
        return fatOffsetSector;
    }

    public long getFatLengthSectors() {
        return fatLengthSectors;
    }

    public long getClusterHeapOffsetSector() {
        return clusterHeapOffsetSector;
    }

    public long getClusterCount() {
        return clusterCount;
    }

    public long getRootDirectoryCluster() {
        return rootDirectoryCluster;
    }

    public long getVolumeSerialNumber() {
        return volumeSerialNumber;
    }

    public long getVolumeLengthSectors() {
        return volumeLengthSectors;
    }

    public long clusterToSector(long clusterNumber, long partitionStartSector) {
        return partitionStartSector + clusterHeapOffsetSector + ((clusterNumber - 2) * sectorsPerCluster);
    }

    public BootSectorInfo toBootSectorInfo() {
        return new BootSectorInfo(
                FileSystemType.EXFAT,
                bytesPerSector,
                sectorsPerCluster,
                volumeLengthSectors,
                "exFAT Volume",
                String.format("%08X", volumeSerialNumber));
    }
}
