package com.recoveryx.filesystem.partition;

import com.recoveryx.common.enumtype.FileSystemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Parses GUID Partition Table (GPT) structures (LBA 1 header & LBA 2+ entry array).
 */
public final class GptPartitionTableParser {

    private static final Logger log = LoggerFactory.getLogger(GptPartitionTableParser.class);

    public static final String GPT_SIGNATURE = "EFI PART";
    public static final int GPT_HEADER_MIN_SIZE = 92;
    public static final int DEFAULT_ENTRY_SIZE = 128;

    // Well-known Partition Type GUIDs
    public static final UUID GUID_UNUSED = new UUID(0L, 0L);
    public static final UUID GUID_BASIC_DATA = UUID.fromString("ebd0a0a2-b9e5-4433-87c0-68b6b72699c7");
    public static final UUID GUID_EFI_SYSTEM = UUID.fromString("c12a7328-f81f-11d2-ba4b-00a0c93ec93b");
    public static final UUID GUID_MS_RESERVED = UUID.fromString("e3c9e316-0b5c-4db8-817d-f92df4021244");
    public static final UUID GUID_LINUX_DATA = UUID.fromString("0fc63daf-8483-4772-8e79-3d69d8477de4");

    public boolean isValidGptHeader(byte[] lba1Data) {
        if (lba1Data == null || lba1Data.length < GPT_HEADER_MIN_SIZE) {
            return false;
        }
        String sig = new String(lba1Data, 0, 8, StandardCharsets.US_ASCII);
        return GPT_SIGNATURE.equals(sig);
    }

    public List<PartitionEntry> parseEntries(byte[] entryArrayData, int entrySize, int bytesPerSector) {
        if (entryArrayData == null || entryArrayData.length < entrySize) {
            return Collections.emptyList();
        }

        ByteBuffer buffer = ByteBuffer.wrap(entryArrayData).order(ByteOrder.LITTLE_ENDIAN);
        int numEntries = entryArrayData.length / entrySize;
        List<PartitionEntry> partitions = new ArrayList<>();

        for (int i = 0; i < numEntries; i++) {
            int offset = i * entrySize;
            if (offset + entrySize > entryArrayData.length) {
                break;
            }

            long typeGuidMost = buffer.getLong(offset);
            long typeGuidLeast = buffer.getLong(offset + 8);

            // If all zeroes, it is an empty partition entry
            if (typeGuidMost == 0 && typeGuidLeast == 0) {
                continue;
            }

            UUID typeGuid = decodeGptGuid(buffer, offset);
            long startLba = buffer.getLong(offset + 32);
            long endLba = buffer.getLong(offset + 40);

            if (startLba < 0 || endLba < startLba) {
                continue;
            }

            long sectorCount = (endLba - startLba) + 1;

            // Partition name: 36 UTF-16LE characters (72 bytes)
            byte[] nameBytes = new byte[72];
            System.arraycopy(entryArrayData, offset + 56, nameBytes, 0, 72);
            String name = new String(nameBytes, StandardCharsets.UTF_16LE).trim().replace("\0", "");

            PartitionType partitionType = resolvePartitionType(typeGuid);
            FileSystemType fsType = partitionType.getDefaultFileSystemType();

            if (name.isBlank()) {
                name = "GPT Partition " + (i + 1) + " (" + partitionType.getDescription() + ")";
            }

            PartitionEntry entry = new PartitionEntry(
                    i,
                    startLba,
                    sectorCount,
                    bytesPerSector,
                    partitionType,
                    fsType,
                    false,
                    name
            );
            partitions.add(entry);
        }

        return Collections.unmodifiableList(partitions);
    }

    public static UUID decodeGptGuid(ByteBuffer buffer, int offset) {
        // GPT GUIDs use mixed-endian representation:
        // Data1 (4 bytes, little-endian)
        // Data2 (2 bytes, little-endian)
        // Data3 (2 bytes, little-endian)
        // Data4 (8 bytes, big-endian)
        int data1 = buffer.getInt(offset);
        short data2 = buffer.getShort(offset + 4);
        short data3 = buffer.getShort(offset + 6);
        long mostSigBits = ((long) (data1 & 0xFFFFFFFFL) << 32)
                | ((long) (data2 & 0xFFFF) << 16)
                | ((long) (data3 & 0xFFFF));

        ByteBuffer beBuffer = buffer.duplicate().order(ByteOrder.BIG_ENDIAN);
        long leastSigBits = beBuffer.getLong(offset + 8);

        return new UUID(mostSigBits, leastSigBits);
    }

    private PartitionType resolvePartitionType(UUID typeGuid) {
        if (GUID_BASIC_DATA.equals(typeGuid)) {
            return PartitionType.GPT_BASIC_DATA;
        } else if (GUID_EFI_SYSTEM.equals(typeGuid)) {
            return PartitionType.GPT_EFI_SYSTEM;
        } else if (GUID_MS_RESERVED.equals(typeGuid)) {
            return PartitionType.GPT_MICROSOFT_RESERVED;
        } else if (GUID_LINUX_DATA.equals(typeGuid)) {
            return PartitionType.MBR_LINUX_NATIVE;
        }
        return PartitionType.UNKNOWN;
    }
}
