package com.recoveryx.license.service;

import com.recoveryx.license.model.LicenseInfo;
import com.recoveryx.license.model.LicenseType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LicenseValidationServiceTest {

    private final HardwareFingerprintService fingerprintService = new HardwareFingerprintService();
    private final LicenseValidationService validationService = new LicenseValidationService(fingerprintService);

    @Test
    void shouldGenerateValidHardwareFingerprint() {
        String hwid = fingerprintService.getHardwareFingerprint();

        assertNotNull(hwid);
        assertFalse(hwid.isBlank());
        assertTrue(hwid.contains("-"), "HWID should contain standard formatting dashes");
    }

    @Test
    void shouldDefaultToCommunityTierForEmptyKey() {
        LicenseInfo info = validationService.validateLicense("");

        assertNotNull(info);
        assertEquals(LicenseType.COMMUNITY, info.type());
        assertTrue(info.valid());
    }

    @Test
    void shouldValidateProLicenseKey() {
        LicenseInfo info = validationService.validateLicense("RCX-PRO-2026-KEY1");

        assertNotNull(info);
        assertEquals(LicenseType.PRO, info.type());
        assertTrue(info.valid());
    }
}
