package com.recoveryx.filesystem.fat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles lookup and traversal of FAT32 File Allocation Table cluster chains.
 */
public final class Fat32Table {

    public static final long FAT32_MASK = 0x0FFFFFFFL;
    public static final long FAT32_EOF_MIN = 0x0FFFFFF8L;
    public static final long FAT32_BAD_CLUSTER = 0x0FFFFFF7L;
    public static final long FAT32_FREE_CLUSTER = 0x00000000L;

    private final byte[] fatTableBytes;

    public Fat32Table(byte[] fatTableBytes) {
        this.fatTableBytes = (fatTableBytes != null) ? fatTableBytes.clone() : new byte[0];
    }

    public long getNextCluster(long currentCluster) {
        int byteOffset = (int) (currentCluster * 4);
        if (byteOffset < 0 || byteOffset + 4 > fatTableBytes.length) {
            return FAT32_EOF_MIN;
        }

        ByteBuffer buffer = ByteBuffer.wrap(fatTableBytes).order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getInt(byteOffset) & FAT32_MASK;
    }

    public boolean isEof(long clusterValue) {
        return (clusterValue & FAT32_MASK) >= FAT32_EOF_MIN;
    }

    public boolean isBad(long clusterValue) {
        return (clusterValue & FAT32_MASK) == FAT32_BAD_CLUSTER;
    }

    public boolean isFree(long clusterValue) {
        return (clusterValue & FAT32_MASK) == FAT32_FREE_CLUSTER;
    }

    public List<Long> getClusterChain(long startingCluster, long maxClusters) {
        if (startingCluster < 2 || isEof(startingCluster) || isFree(startingCluster)) {
            return Collections.emptyList();
        }

        List<Long> chain = new ArrayList<>();
        long current = startingCluster;
        long count = 0;

        while (current >= 2 && !isEof(current) && !isBad(current) && count < maxClusters) {
            chain.add(current);
            count++;

            long next = getNextCluster(current);
            if (next == current || chain.contains(next)) {
                // Cycle detected
                break;
            }
            current = next;
        }

        return Collections.unmodifiableList(chain);
    }
}
