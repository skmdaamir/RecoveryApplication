package com.recoveryx.license.service;

import com.recoveryx.common.util.ValidationUtils;
import com.recoveryx.license.model.LicenseInfo;
import com.recoveryx.license.model.LicenseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Validates license keys and checks tier capabilities.
 */
public final class LicenseValidationService {

    private static final Logger log = LoggerFactory.getLogger(LicenseValidationService.class);

    private final HardwareFingerprintService fingerprintService;

    public LicenseValidationService(HardwareFingerprintService fingerprintService) {
        this.fingerprintService = ValidationUtils.requireNonNull(fingerprintService, "fingerprintService");
    }

    /**
     * Validates a product license key string.
     *
     * @param licenseKey product key string (e.g. "RCX-PRO-2026-KEY1")
     * @return LicenseInfo status object
     */
    public LicenseInfo validateLicense(String licenseKey) {
        String hwid = fingerprintService.getHardwareFingerprint();

        if (licenseKey == null || licenseKey.isBlank()) {
            return new LicenseInfo("FREE-COMMUNITY", LicenseType.COMMUNITY, "Community User", Instant.MAX, true, hwid);
        }

        String cleanKey = licenseKey.toUpperCase().trim();

        if (cleanKey.startsWith("RCX-PRO") || cleanKey.contains("PRO")) {
            return new LicenseInfo(cleanKey, LicenseType.PRO, "Registered Pro User", Instant.now().plus(365, ChronoUnit.DAYS), true, hwid);
        }

        if (cleanKey.startsWith("RCX-ENT") || cleanKey.contains("ENTERPRISE")) {
            return new LicenseInfo(cleanKey, LicenseType.ENTERPRISE, "Enterprise Client", Instant.now().plus(730, ChronoUnit.DAYS), true, hwid);
        }

        log.warn("Invalid license key format: {}", licenseKey);
        return new LicenseInfo(cleanKey, LicenseType.COMMUNITY, "Community User", Instant.MAX, false, hwid);
    }
}
