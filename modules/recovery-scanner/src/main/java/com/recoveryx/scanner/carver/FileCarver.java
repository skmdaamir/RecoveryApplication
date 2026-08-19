package com.recoveryx.scanner.carver;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.common.enumtype.HealthStatus;
import com.recoveryx.common.enumtype.RecoveryChance;
import com.recoveryx.common.util.IdGenerator;
import com.recoveryx.core.domain.file.FileFragment;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.scanner.signature.FileSignature;
import com.recoveryx.scanner.signature.SignatureDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Scans a raw byte buffer (one or more contiguous sectors) to identify files by
 * their magic header bytes and extracts them as RecoverableFile objects.
 */
public final class FileCarver {

    private static final Logger log = LoggerFactory.getLogger(FileCarver.class);

    private static final int SECTOR_SIZE = 512;

    private final SignatureDatabase signatureDatabase;

    public FileCarver(SignatureDatabase signatureDatabase) {
        this.signatureDatabase = Objects.requireNonNull(signatureDatabase, "signatureDatabase");
    }

    /**
     * Carves recoverable files out of a contiguous raw sector buffer.
     *
     * @param data              the raw byte data to scan
     * @param startOffsetBytes  absolute byte offset where this buffer begins on the device
     * @return list of discovered RecoverableFile candidates
     */
    public List<RecoverableFile> carve(byte[] data, long startOffsetBytes) {
        if (data == null || data.length < SECTOR_SIZE) {
            return Collections.emptyList();
        }

        List<RecoverableFile> results = new ArrayList<>();
        int len = data.length;

        for (int offset = 0; offset <= len - 4; offset += SECTOR_SIZE) {
            List<FileSignature> matches = signatureDatabase.match(data, offset);
            for (FileSignature sig : matches) {
                long carveStart = startOffsetBytes + offset;
                long estimatedSize = estimateSize(data, offset, sig);

                RecoverableFile file = buildCarvedFile(sig, carveStart, estimatedSize, offset);
                results.add(file);
                log.debug("Carved {} at offset {}", sig.getSignatureName(), carveStart);
            }
        }

        return Collections.unmodifiableList(results);
    }

    private long estimateSize(byte[] data, int headerOffset, FileSignature sig) {
        // If signature has a footer, search for it
        if (sig.hasFooter()) {
            byte[] footer = sig.getFooterMagic();
            int searchEnd = (int) Math.min(data.length - footer.length, headerOffset + sig.getMaxSizeBytes());

            for (int i = headerOffset + 2; i <= searchEnd - footer.length + 1; i++) {
                boolean found = true;
                for (int j = 0; j < footer.length; j++) {
                    if (data[i + j] != footer[j]) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    return (long)(i - headerOffset) + footer.length;
                }
            }
        }

        // Default: cap at maxSizeBytes or remaining buffer
        long remaining = (long) data.length - headerOffset;
        return Math.min(sig.getMaxSizeBytes(), Math.max(SECTOR_SIZE, remaining));
    }

    private RecoverableFile buildCarvedFile(FileSignature sig, long startByte, long size, int dataOffset) {
        String name = "carved_" + sig.getExtension() + "_" + Long.toHexString(startByte).toUpperCase() + "." + sig.getExtension();
        List<FileFragment> fragments = new ArrayList<>();
        fragments.add(new FileFragment(startByte, size, 0));

        HealthStatus health = size > SECTOR_SIZE ? HealthStatus.GOOD : HealthStatus.FAIR;
        RecoveryChance chance = sig.getConfidence() >= 95 ? RecoveryChance.HIGH : RecoveryChance.MEDIUM;

        return new RecoverableFile(
                "carve-" + IdGenerator.newId(),
                name,
                sig.getExtension(),
                sig.getCategory(),
                "/" + name,
                null,
                size,
                Instant.now(),
                null,
                null,
                health,
                chance,
                isPreviewable(sig.getCategory()),
                false,
                fragments,
                null);
    }

    private static boolean isPreviewable(FileCategory category) {
        return category == FileCategory.IMAGE || category == FileCategory.RAW_IMAGE
                || category == FileCategory.PDF || category == FileCategory.AUDIO;
    }
}
