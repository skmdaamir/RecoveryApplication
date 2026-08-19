package com.recoveryx.filesystem.ntfs;

import com.recoveryx.core.domain.file.FileFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Decodes compressed NTFS Data Runs into physical/logical FileFragment disk extents.
 */
public final class NtfsDataRunDecoder {

    private static final Logger log = LoggerFactory.getLogger(NtfsDataRunDecoder.class);

    /**
     * Decodes NTFS Data Runs from a byte buffer starting at a given offset.
     *
     * @param buffer byte buffer containing the run list
     * @param runListOffset offset where data runs begin
     * @param runListLength maximum bytes available for the run list
     * @param clusterSizeBytes cluster size in bytes (e.g. 4096)
     * @param partitionStartOffsetBytes starting byte offset of the partition on the physical drive
     * @return list of decoded FileFragment records representing the file's disk allocation
     */
    public static List<FileFragment> decode(
            byte[] buffer,
            int runListOffset,
            int runListLength,
            int clusterSizeBytes,
            long partitionStartOffsetBytes) {

        if (buffer == null || runListOffset < 0 || runListOffset >= buffer.length || clusterSizeBytes <= 0) {
            return Collections.emptyList();
        }

        List<FileFragment> fragments = new ArrayList<>();
        int currentOffset = runListOffset;
        int maxOffset = Math.min(buffer.length, runListOffset + runListLength);
        long currentLcn = 0; // Cumulative Logical Cluster Number
        int sequenceOrder = 0;

        while (currentOffset < maxOffset) {
            byte header = buffer[currentOffset++];
            if (header == 0) {
                // 0x00 marks the end of data runs
                break;
            }

            int lengthBytesCount = header & 0x0F;
            int offsetBytesCount = (header >> 4) & 0x0F;

            if (lengthBytesCount == 0 || currentOffset + lengthBytesCount + offsetBytesCount > maxOffset) {
                log.debug("Malformed data run header 0x{:02X} at offset {}", header, currentOffset - 1);
                break;
            }

            // Decode Run Length in clusters (unsigned)
            long clusterCount = 0;
            for (int i = 0; i < lengthBytesCount; i++) {
                clusterCount |= ((long) (buffer[currentOffset++] & 0xFF)) << (i * 8);
            }

            // Decode Run Offset (signed relative delta)
            if (offsetBytesCount == 0) {
                // Sparse run (compressed / sparse zeroes)
                long fragmentSizeBytes = clusterCount * clusterSizeBytes;
                fragments.add(new FileFragment(0, fragmentSizeBytes, sequenceOrder++));
            } else {
                long clusterDelta = 0;
                for (int i = 0; i < offsetBytesCount; i++) {
                    clusterDelta |= ((long) (buffer[currentOffset++] & 0xFF)) << (i * 8);
                }

                // Sign-extend if negative
                int signBitIndex = (offsetBytesCount * 8) - 1;
                if ((clusterDelta & (1L << signBitIndex)) != 0) {
                    long signMask = -1L << (offsetBytesCount * 8);
                    clusterDelta |= signMask;
                }

                currentLcn += clusterDelta;
                long physicalStartOffset = partitionStartOffsetBytes + (currentLcn * clusterSizeBytes);
                long fragmentSizeBytes = clusterCount * clusterSizeBytes;

                fragments.add(new FileFragment(physicalStartOffset, fragmentSizeBytes, sequenceOrder++));
            }
        }

        return Collections.unmodifiableList(fragments);
    }
}
