package com.recoveryx.filesystem.partition;

import com.recoveryx.common.enumtype.FileSystemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parses standard Master Boot Record (MBR) partition tables (LBA 0).
 */
public final class MbrPartitionTableParser {

    private static final Logger log = LoggerFactory.getLogger(MbrPartitionTableParser.class);

    public static final int MBR_SECTOR_SIZE = 512;
    public static final int MBR_SIGNATURE_OFFSET = 510;
    public static final int PARTITION_TABLE_OFFSET = 446;
    public static final int ENTRY_SIZE = 16;
    public static final int NUM_ENTRIES = 4;
    public static final int BOOT_SIGNATURE = 0xAA55;

    public boolean isValidMbr(byte[] sector0) {
        if (sector0 == null || sector0.length < MBR_SECTOR_SIZE) {
            return false;
        }
        int sig = (sector0[MBR_SIGNATURE_OFFSET] & 0xFF) | ((sector0[MBR_SIGNATURE_OFFSET + 1] & 0xFF) << 8);
        return sig == BOOT_SIGNATURE;
    }

    public boolean isGptProtectiveMbr(byte[] sector0) {
        if (!isValidMbr(sector0)) {
            return false;
        }
        List<PartitionEntry> entries = parse(sector0, MBR_SECTOR_SIZE);
        return entries.stream().anyMatch(e -> e.partitionType() == PartitionType.MBR_GPT_PROTECTIVE);
    }

    public List<PartitionEntry> parse(byte[] sector0, int bytesPerSector) {
        if (!isValidMbr(sector0)) {
            log.debug("Sector 0 is not a valid MBR");
            return Collections.emptyList();
        }

        ByteBuffer buffer = ByteBuffer.wrap(sector0).order(ByteOrder.LITTLE_ENDIAN);
        List<PartitionEntry> partitions = new ArrayList<>();

        for (int i = 0; i < NUM_ENTRIES; i++) {
            int offset = PARTITION_TABLE_OFFSET + (i * ENTRY_SIZE);
            byte bootFlag = buffer.get(offset);
            byte typeCode = buffer.get(offset + 4);
            long startLba = Integer.toUnsignedLong(buffer.getInt(offset + 8));
            long sectorCount = Integer.toUnsignedLong(buffer.getInt(offset + 12));

            if (typeCode == 0 || sectorCount == 0) {
                continue; // Unused entry
            }

            PartitionType partitionType = PartitionType.fromMbrType(typeCode);
            FileSystemType fsType = partitionType.getDefaultFileSystemType();
            boolean isBootable = (bootFlag & 0x80) != 0;

            PartitionEntry entry = new PartitionEntry(
                    i,
                    startLba,
                    sectorCount,
                    bytesPerSector,
                    partitionType,
                    fsType,
                    isBootable,
                    "MBR Partition " + (i + 1) + " (" + partitionType.getDescription() + ")"
            );
            partitions.add(entry);
        }

        return Collections.unmodifiableList(partitions);
    }
}
