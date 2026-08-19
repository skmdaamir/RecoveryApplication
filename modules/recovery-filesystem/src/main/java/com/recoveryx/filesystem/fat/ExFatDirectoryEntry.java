package com.recoveryx.filesystem.fat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Parses exFAT 32-byte directory entry records (Primary File, Stream Extension, File Name).
 */
public final class ExFatDirectoryEntry {

    public static final byte TYPE_FILE = (byte) 0x85;
    public static final byte TYPE_STREAM_EXTENSION = (byte) 0xC0;
    public static final byte TYPE_FILE_NAME = (byte) 0xC1;
    public static final byte IN_USE_MASK = (byte) 0x80;

    private final String fileName;
    private final boolean deleted;
    private final boolean directory;
    private final long firstCluster;
    private final long dataLength;
    private final boolean noFatChain;
    private final Instant creationTime;
    private final Instant modificationTime;

    public ExFatDirectoryEntry(
            String fileName,
            boolean deleted,
            boolean directory,
            long firstCluster,
            long dataLength,
            boolean noFatChain,
            Instant creationTime,
            Instant modificationTime) {
        this.fileName = fileName == null ? "" : fileName;
        this.deleted = deleted;
        this.directory = directory;
        this.firstCluster = firstCluster;
        this.dataLength = dataLength;
        this.noFatChain = noFatChain;
        this.creationTime = creationTime;
        this.modificationTime = modificationTime;
    }

    public static boolean isFileEntry(byte typeByte) {
        return (typeByte & 0x7F) == (TYPE_FILE & 0x7F);
    }

    public static boolean isStreamExtension(byte typeByte) {
        return (typeByte & 0x7F) == (TYPE_STREAM_EXTENSION & 0x7F);
    }

    public static boolean isFileName(byte typeByte) {
        return (typeByte & 0x7F) == (TYPE_FILE_NAME & 0x7F);
    }

    public static String parseFileNamePiece(byte[] nameEntry) {
        if (nameEntry == null || nameEntry.length < 32) {
            return "";
        }
        // Characters 0-14 (bytes 2-31, 30 bytes UTF-16LE)
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < 32; i += 2) {
            int c = (nameEntry[i] & 0xFF) | ((nameEntry[i + 1] & 0xFF) << 8);
            if (c == 0x0000 || c == 0xFFFF) {
                break;
            }
            sb.append((char) c);
        }
        return sb.toString();
    }

    public static Instant parseExFatDateTime(int dosDate, int dosTime, int tenMs) {
        Instant base = Fat32DirectoryEntry.dosDateTimeToInstant(dosDate, dosTime);
        if (tenMs > 0 && tenMs < 200) {
            return base.plusMillis(tenMs * 10L);
        }
        return base;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isDirectory() {
        return directory;
    }

    public long getFirstCluster() {
        return firstCluster;
    }

    public long getDataLength() {
        return dataLength;
    }

    public boolean isNoFatChain() {
        return noFatChain;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public Instant getModificationTime() {
        return modificationTime;
    }
}
