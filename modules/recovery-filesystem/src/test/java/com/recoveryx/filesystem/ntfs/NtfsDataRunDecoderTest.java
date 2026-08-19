package com.recoveryx.filesystem.ntfs;

import com.recoveryx.core.domain.file.FileFragment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NtfsDataRunDecoderTest {

    @Test
    void shouldDecodeSinglePositiveDataRun() {
        // Run: length 16 (0x10) clusters at cluster 1000 (0x03E8)
        // Header: low nibble 1 (1 byte length), high nibble 2 (2 bytes offset) -> 0x21
        byte[] runs = new byte[] {
                (byte) 0x21,
                (byte) 0x10, // length = 16 clusters
                (byte) 0xE8, (byte) 0x03, // offset = +1000
                (byte) 0x00 // End marker
        };

        List<FileFragment> fragments = NtfsDataRunDecoder.decode(runs, 0, runs.length, 4096, 0);

        assertEquals(1, fragments.size());
        FileFragment f1 = fragments.get(0);
        assertEquals(1000L * 4096, f1.startOffsetBytes());
        assertEquals(16L * 4096, f1.lengthBytes());
        assertEquals(0, f1.sequenceOrder());
    }

    @Test
    void shouldDecodeFragmentedRunsWithNegativeClusterDelta() {
        // Run 1: +1000 clusters, length 16 -> 0x21, 0x10, 0xE8, 0x03
        // Run 2: -10 clusters (0xF6), length 8 -> 0x11, 0x08, 0xF6
        // End marker: 0x00
        byte[] runs = new byte[] {
                (byte) 0x21, (byte) 0x10, (byte) 0xE8, (byte) 0x03,
                (byte) 0x11, (byte) 0x08, (byte) 0xF6,
                (byte) 0x00
        };

        List<FileFragment> fragments = NtfsDataRunDecoder.decode(runs, 0, runs.length, 4096, 1048576L);

        assertEquals(2, fragments.size());

        FileFragment f1 = fragments.get(0);
        assertEquals(1048576L + (1000L * 4096), f1.startOffsetBytes());
        assertEquals(16L * 4096, f1.lengthBytes());
        assertEquals(0, f1.sequenceOrder());

        FileFragment f2 = fragments.get(1);
        // Current LCN = 1000 + (-10) = 990
        assertEquals(1048576L + (990L * 4096), f2.startOffsetBytes());
        assertEquals(8L * 4096, f2.lengthBytes());
        assertEquals(1, f2.sequenceOrder());
    }
}
