package com.recoveryx.preview.model;

import com.recoveryx.common.util.ValidationUtils;

import java.util.List;

/**
 * Immutable container for file preview rendering data.
 *
 * @param previewType    type of preview generated
 * @param thumbnailBytes PNG/JPEG image bytes for thumbnails (if image)
 * @param textSnippet    plain text content (if text/code/document)
 * @param hexDumpLines   formatted 16-byte hex dump lines (if hex/binary)
 * @param fileSize       total file size in bytes
 * @param truncated      whether the preview data is truncated
 */
public record PreviewData(
        PreviewType previewType,
        byte[] thumbnailBytes,
        String textSnippet,
        List<String> hexDumpLines,
        long fileSize,
        boolean truncated) {

    public PreviewData {
        ValidationUtils.requireNonNull(previewType, "previewType");
        thumbnailBytes = thumbnailBytes != null ? thumbnailBytes.clone() : new byte[0];
        hexDumpLines = List.copyOf(hexDumpLines == null ? List.of() : hexDumpLines);
    }
}
