package com.recoveryx.core.domain.scan;

import com.recoveryx.common.enumtype.ScanMode;
import com.recoveryx.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ScanRequestTest {

    @Test
    void shouldRejectEmptyScanModes() {
        assertThrows(ValidationException.class,
                () -> new ScanRequest("scan-1", "device-1", null, Set.of(), true, false, false, 4, 1024));
    }

    @Test
    void shouldRejectInvalidThreadCount() {
        assertThrows(ValidationException.class, () -> new ScanRequest("scan-1", "device-1", null,
                Set.of(ScanMode.QUICK_SCAN), true, false, false, 0, 1024));
    }
}