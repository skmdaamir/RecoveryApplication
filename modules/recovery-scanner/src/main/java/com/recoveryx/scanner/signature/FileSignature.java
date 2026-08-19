package com.recoveryx.scanner.signature;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.common.util.ValidationUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * Defines a file signature (magic header/footer bytes and size constraints) for signature-based carving.
 */
public final class FileSignature {

    private final String extension;
    private final FileCategory category;
    private final String signatureName;
    private final byte[] headerMagic;
    private final int headerOffset;
    private final byte[] footerMagic;
    private final long maxSizeBytes;
    private final int confidence;

    public FileSignature(
            String extension,
            FileCategory category,
            String signatureName,
            byte[] headerMagic,
            int headerOffset,
            byte[] footerMagic,
            long maxSizeBytes,
            int confidence) {
        this.extension = ValidationUtils.requireNotBlank(extension, "extension").toLowerCase();
        this.category = ValidationUtils.requireNonNull(category, "category");
        this.signatureName = ValidationUtils.requireNotBlank(signatureName, "signatureName");
        this.headerMagic = Objects.requireNonNull(headerMagic, "headerMagic").clone();
        this.headerOffset = Math.max(0, headerOffset);
        this.footerMagic = footerMagic != null ? footerMagic.clone() : new byte[0];
        this.maxSizeBytes = Math.max(512, maxSizeBytes);
        this.confidence = Math.max(0, Math.min(100, confidence));
    }

    public boolean matchesHeader(byte[] data, int offset) {
        if (data == null || offset < 0 || offset + headerOffset + headerMagic.length > data.length) {
            return false;
        }
        for (int i = 0; i < headerMagic.length; i++) {
            if (data[offset + headerOffset + i] != headerMagic[i]) {
                return false;
            }
        }
        return true;
    }

    public String getExtension() {
        return extension;
    }

    public FileCategory getCategory() {
        return category;
    }

    public String getSignatureName() {
        return signatureName;
    }

    public byte[] getHeaderMagic() {
        return headerMagic.clone();
    }

    public int getHeaderOffset() {
        return headerOffset;
    }

    public byte[] getFooterMagic() {
        return footerMagic.clone();
    }

    public boolean hasFooter() {
        return footerMagic.length > 0;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public int getConfidence() {
        return confidence;
    }
}
