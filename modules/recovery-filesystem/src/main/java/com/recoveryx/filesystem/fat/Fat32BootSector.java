package com.recoveryx.filesystem.fat;

import com.recoveryx.common.enumtype.FileSystemType;
import com.recoveryx.common.util.ValidationUtils;
import com.recoveryx.filesystem.common.BootSectorInfo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Parses and models the FAT32 Boot Sector & BIOS Parameter Block (BPB).
 */
public final class Fat32BootSector {

    private final int bytesPerSector;
    private final int sectorsPerCluster;
    private final int reservedSectors;
    private final int numberOfFats;
    private final long totalSectors;
    private final long sectorsPerFat;
    private final long rootDirectoryCluster;
    private final int fsInfoSector;
    private final int backupBootSector;
    private final long volumeSerialNumber;
    private final String volumeLabel;

    private Fat32BootSector(
            int bytesPerSector,
            int sectorsPerCluster,
            int reservedSectors,
            int numberOfFats,
            long totalSectors,
            long sectorsPerFat,
            long rootDirectoryCluster,
            int fsInfoSector,
            int backupBootSector,
            long volumeSerialNumber,
            String volumeLabel) {
        this.bytesPerSector = bytesPerSector;
        this.sectorsPerCluster = sectorsPerCluster;
        this.reservedSectors = reservedSectors;
        this.numberOfFats = numberOfFats;
        this.totalSectors = totalSectors;
        this.sectorsPerFat = sectorsPerFat;
        this.rootDirectoryCluster = rootDirectoryCluster;
        this.fsInfoSector = fsInfoSector;
        this.backupBootSector = backupBootSector;
        this.volumeSerialNumber = volumeSerialNumber;
        this.volumeLabel = volumeLabel;
    }

    public static Fat32BootSector parse(byte[] sector0) {
        ValidationUtils.requireNonNull(sector0, "sector0");
        if (sector0.length < 512) {
            throw new IllegalArgumentException("FAT32 boot sector must be at least 512 bytes");
        }

        ByteBuffer buffer = ByteBuffer.wrap(sector0).order(ByteOrder.LITTLE_ENDIAN);

        int bytesPerSector = buffer.getShort(0x0B) & 0xFFFF;
        if (bytesPerSector <= 0) {
            bytesPerSector = 512;
        }

        int sectorsPerCluster = buffer.get(0x0D) & 0xFF;
        if (sectorsPerCluster <= 0) {
            sectorsPerCluster = 8;
        }

        int reservedSectors = buffer.getShort(0x0E) & 0xFFFF;
        int numberOfFats = buffer.get(0x10) & 0xFF;

        long totalSectors = buffer.getShort(0x13) & 0xFFFF;
        if (totalSectors == 0) {
            totalSectors = Integer.toUnsignedLong(buffer.getInt(0x20));
        }

        long sectorsPerFat = Integer.toUnsignedLong(buffer.getInt(0x24));
        long rootCluster = Integer.toUnsignedLong(buffer.getInt(0x2C));
        int fsInfo = buffer.getShort(0x30) & 0xFFFF;
        int backupBoot = buffer.getShort(0x32) & 0xFFFF;

        long serialNumber = Integer.toUnsignedLong(buffer.getInt(0x43));

        byte[] labelBytes = new byte[11];
        System.arraycopy(sector0, 0x47, labelBytes, 0, 11);
        String label = new String(labelBytes, StandardCharsets.US_ASCII).trim();

        return new Fat32BootSector(
                bytesPerSector,
                sectorsPerCluster,
                reservedSectors,
                numberOfFats,
                totalSectors,
                sectorsPerFat,
                rootCluster > 0 ? rootCluster : 2,
                fsInfo,
                backupBoot,
                serialNumber,
                label);
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

    public int getReservedSectors() {
        return reservedSectors;
    }

    public int getNumberOfFats() {
        return numberOfFats;
    }

    public long getTotalSectors() {
        return totalSectors;
    }

    public long getSectorsPerFat() {
        return sectorsPerFat;
    }

    public long getRootDirectoryCluster() {
        return rootDirectoryCluster;
    }

    public long getFatStartSector(long partitionStartSector) {
        return partitionStartSector + reservedSectors;
    }

    public long getClusterHeapStartSector(long partitionStartSector) {
        return partitionStartSector + reservedSectors + (numberOfFats * sectorsPerFat);
    }

    public long clusterToSector(long clusterNumber, long partitionStartSector) {
        long firstDataSector = getClusterHeapStartSector(partitionStartSector);
        return firstDataSector + ((clusterNumber - 2) * sectorsPerCluster);
    }

    public BootSectorInfo toBootSectorInfo() {
        return new BootSectorInfo(
                FileSystemType.FAT32,
                bytesPerSector,
                sectorsPerCluster,
                totalSectors,
                volumeLabel,
                String.format("%08X", volumeSerialNumber));
    }
}
