package com.recoveryx.engine.integrity;

import com.recoveryx.common.util.ValidationUtils;

/**
 * Immutable report representing the checksum and structural integrity of a recovered file.
 *
 * @param md5Hash           calculated MD5 hex digest
 * @param sha256Hash        calculated SHA-256 hex digest
 * @param validHeader       whether valid magic header was verified
 * @param validFooter       whether valid magic footer was verified
 * @param corrupt           whether structural corruption was detected
 * @param statusDescription summary status explanation
 */
public record IntegrityReport(
        String md5Hash,
        String sha256Hash,
        boolean validHeader,
        boolean validFooter,
        boolean corrupt,
        String statusDescription) {

    public IntegrityReport {
        ValidationUtils.requireNotBlank(md5Hash, "md5Hash");
        ValidationUtils.requireNotBlank(sha256Hash, "sha256Hash");
        ValidationUtils.requireNotBlank(statusDescription, "statusDescription");
    }
}
