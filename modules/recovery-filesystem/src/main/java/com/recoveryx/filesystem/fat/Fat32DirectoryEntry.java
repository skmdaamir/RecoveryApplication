package com.recoveryx.filesystem.fat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Parses and models FAT32 32-byte directory entries and LFN chains.
 */
public final class Fat32DirectoryEntry {

    public static final int ENTRY_SIZE = 32;
    public static final byte DELETED_MARKER = (byte) 0xE5;
    public static final byte END_OF_DIR_MARKER = 0x00;
    public static final byte ATTR_LFN = 0x0F;
    public static final byte ATTR_DIRECTORY = 0x10;
    public static final byte ATTR_VOLUME_ID = 0x08;

    private final String shortName;
    private final String longName;
    private final byte attributes;
    private final boolean deleted;
    private final boolean directory;
    private final boolean volumeId;
    private final long startingCluster;
    private final long fileSize;
    private final Instant creationTime;
    private final Instant modificationTime;

    public Fat32DirectoryEntry(
            String shortName,
            String longName,
            byte attributes,
            boolean deleted,
            boolean directory,
            boolean volumeId,
            long startingCluster,
            long fileSize,
            Instant creationTime,
            Instant modificationTime) {
        this.shortName = shortName;
        this.longName = longName;
        this.attributes = attributes;
        this.deleted = deleted;
        this.directory = directory;
        this.volumeId = volumeId;
        this.startingCluster = startingCluster;
        this.fileSize = fileSize;
        this.creationTime = creationTime;
        this.modificationTime = modificationTime;
    }

    public static Fat32DirectoryEntry parse(byte[] entryData, String currentLfn) {
        if (entryData == null || entryData.length < ENTRY_SIZE) {
            return null;
        }

        byte firstByte = entryData[0];
        if (firstByte == END_OF_DIR_MARKER) {
            return null; // End of directory
        }

        boolean isDeleted = (firstByte == DELETED_MARKER);
        byte attr = entryData[11];
        if (attr == ATTR_LFN) {
            return null; // Handled separately in LFN accumulator
        }

        boolean isDir = (attr & ATTR_DIRECTORY) != 0;
        boolean isVolId = (attr & ATTR_VOLUME_ID) != 0;

        ByteBuffer buffer = ByteBuffer.wrap(entryData).order(ByteOrder.LITTLE_ENDIAN);

        // Extract 8.3 name
        byte[] nameBytes = new byte[8];
        System.arraycopy(entryData, 0, nameBytes, 0, 8);
        if (isDeleted) {
            nameBytes[0] = '_'; // Replace deleted 0xE5 with underscore for readability
        }
        String baseName = new String(nameBytes, StandardCharsets.US_ASCII).trim();

        byte[] extBytes = new byte[3];
        System.arraycopy(entryData, 8, extBytes, 0, 3);
        String ext = new String(extBytes, StandardCharsets.US_ASCII).trim();

        String shortName = ext.isEmpty() ? baseName : baseName + "." + ext;
        String finalName = (currentLfn != null && !currentLfn.isBlank()) ? currentLfn : shortName;

        int createTime = buffer.getShort(14) & 0xFFFF;
        int createDate = buffer.getShort(16) & 0xFFFF;
        int modTime = buffer.getShort(22) & 0xFFFF;
        int modDate = buffer.getShort(24) & 0xFFFF;

        int clusterHigh = buffer.getShort(20) & 0xFFFF;
        int clusterLow = buffer.getShort(26) & 0xFFFF;
        long startingCluster = ((long) clusterHigh << 16) | clusterLow;

        long fileSize = Integer.toUnsignedLong(buffer.getInt(28));

        Instant created = dosDateTimeToInstant(createDate, createTime);
        Instant modified = dosDateTimeToInstant(modDate, modTime);

        return new Fat32DirectoryEntry(
                shortName,
                finalName,
                attr,
                isDeleted,
                isDir,
                isVolId,
                startingCluster,
                fileSize,
                created,
                modified);
    }

    public static String parseLfnPiece(byte[] lfnEntry) {
        if (lfnEntry == null || lfnEntry.length < ENTRY_SIZE || lfnEntry[11] != ATTR_LFN) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        // Chars 1-5 (bytes 1-10)
        appendChars(sb, lfnEntry, 1, 5);
        // Chars 6-11 (bytes 14-25)
        appendChars(sb, lfnEntry, 14, 6);
        // Chars 12-13 (bytes 28-31)
        appendChars(sb, lfnEntry, 28, 2);

        return sb.toString();
    }

    private static void appendChars(StringBuilder sb, byte[] data, int offset, int charCount) {
        for (int i = 0; i < charCount; i++) {
            int pos = offset + (i * 2);
            if (pos + 1 >= data.length) {
                break;
            }
            int c = (data[pos] & 0xFF) | ((data[pos + 1] & 0xFF) << 8);
            if (c == 0x0000 || c == 0xFFFF) {
                break;
            }
            sb.append((char) c);
        }
    }

    public static Instant dosDateTimeToInstant(int dosDate, int dosTime) {
        if (dosDate == 0) {
            return Instant.EPOCH;
        }
        int year = 1980 + ((dosDate >> 9) & 0x7F);
        int month = Math.max(1, Math.min(12, (dosDate >> 5) & 0x0F));
        int day = Math.max(1, Math.min(31, dosDate & 0x1F));

        int hour = Math.max(0, Math.min(23, (dosTime >> 11) & 0x1F));
        int minute = Math.max(0, Math.min(59, (dosTime >> 5) & 0x3F));
        int second = Math.max(0, Math.min(58, (dosTime & 0x1F) * 2));

        try {
            return LocalDateTime.of(year, month, day, hour, minute, second).toInstant(ZoneOffset.UTC);
        } catch (Exception e) {
            return Instant.EPOCH;
        }
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public byte getAttributes() {
        return attributes;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isDirectory() {
        return directory;
    }

    public boolean isVolumeId() {
        return volumeId;
    }

    public long getStartingCluster() {
        return startingCluster;
    }

    public long getFileSize() {
        return fileSize;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public Instant getModificationTime() {
        return modificationTime;
    }
}
