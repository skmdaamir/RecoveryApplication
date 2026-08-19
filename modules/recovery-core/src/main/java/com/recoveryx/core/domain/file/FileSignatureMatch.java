package com.recoveryx.core.domain.file;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.common.util.ValidationUtils;

/**
 * Signature-based identification result for a file candidate.
 *
 * @param extension detected extension
 * @param category detected file category
 * @param signatureName signature label
 * @param confidence confidence between 0 and 100
 */
public record FileSignatureMatch(
        String extension,
        FileCategory category,
        String signatureName,
        int confidence) {

    public FileSignatureMatch {
        ValidationUtils.requireNotBlank(extension, "extension");
        ValidationUtils.requireNonNull(category, "category");
        ValidationUtils.requireNotBlank(signatureName, "signatureName");
        if (confidence < 0 || confidence > 100) {
            throw new IllegalArgumentException("confidence must be between 0 and 100");
        }
    }
}