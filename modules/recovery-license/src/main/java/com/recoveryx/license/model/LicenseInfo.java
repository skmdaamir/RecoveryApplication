package com.recoveryx.license.model;

import com.recoveryx.common.util.ValidationUtils;

import java.time.Instant;

/**
 * Immutable license payload metadata.
 */
public record LicenseInfo(
        String licenseKey,
        LicenseType type,
        String ownerName,
        Instant expirationDate,
        boolean valid,
        String hwid) {

    public LicenseInfo {
        ValidationUtils.requireNotBlank(licenseKey, "licenseKey");
        ValidationUtils.requireNonNull(type, "type");
        ValidationUtils.requireNotBlank(ownerName, "ownerName");
        ValidationUtils.requireNotBlank(hwid, "hwid");
    }
}
