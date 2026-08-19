package com.recoveryx.common.util;

import com.recoveryx.common.enumtype.ChecksumAlgorithm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChecksumUtilsTest {

    @Test
    void shouldGenerateSha256Checksum() {
        String checksum = ChecksumUtils.checksum("RecoveryX", ChecksumAlgorithm.SHA256);
        assertEquals("0EF73946661B5EFDDDA2B3AA7B54B2EC4AB7B496EB390CD1346AC1FAB6AFCA34", checksum);
    }
}