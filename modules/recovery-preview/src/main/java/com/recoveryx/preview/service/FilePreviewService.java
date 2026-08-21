package com.recoveryx.preview.service;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.preview.model.PreviewData;
import com.recoveryx.preview.model.PreviewType;
import com.recoveryx.preview.util.HexDumpGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Service for generating image thumbnails, text snippets, and hex dump previews for recoverable files.
 */
public final class FilePreviewService {

    private static final Logger log = LoggerFactory.getLogger(FilePreviewService.class);

    private static final int MAX_TEXT_PREVIEW_BYTES = 4096;
    private static final int MAX_HEX_PREVIEW_BYTES = 2048;
    private static final int THUMBNAIL_MAX_SIZE = 300;

    public FilePreviewService() {
    }

    /**
     * Generates a preview payload for a file candidate given its raw byte content.
     *
     * @param file      recoverable file metadata
     * @param rawData   file byte content
     * @return PreviewData payload
     */
    public PreviewData generatePreview(RecoverableFile file, byte[] rawData) {
        if (file == null || rawData == null || rawData.length == 0) {
            return new PreviewData(
                    PreviewType.UNSUPPORTED,
                    new byte[0],
                    "No file content available for preview.",
                    List.of(),
                    file != null ? file.fileSize() : 0,
                    false);
        }

        FileCategory cat = file.category();

        if (cat == FileCategory.IMAGE || cat == FileCategory.RAW_IMAGE) {
            byte[] thumb = createThumbnail(rawData);
            if (thumb.length > 0) {
                return new PreviewData(PreviewType.IMAGE, thumb, null, List.of(), rawData.length, false);
            }
        }

        if (cat == FileCategory.DOCUMENT || cat == FileCategory.SOURCE_CODE) {
            String text = createTextSnippet(rawData);
            boolean truncated = rawData.length > MAX_TEXT_PREVIEW_BYTES;
            return new PreviewData(PreviewType.TEXT, new byte[0], text, List.of(), rawData.length, truncated);
        }

        // Fallback: Generate Hex Dump for binary or unknown formats
        List<String> hexLines = HexDumpGenerator.generateHexDump(rawData, MAX_HEX_PREVIEW_BYTES);
        boolean truncated = rawData.length > MAX_HEX_PREVIEW_BYTES;
        return new PreviewData(PreviewType.HEX, new byte[0], null, hexLines, rawData.length, truncated);
    }

    private byte[] createThumbnail(byte[] imageBytes) {
        try {
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (original == null) {
                return new byte[0];
            }

            int origWidth = original.getWidth();
            int origHeight = original.getHeight();

            double scale = Math.min(
                    (double) THUMBNAIL_MAX_SIZE / origWidth,
                    (double) THUMBNAIL_MAX_SIZE / origHeight);

            int targetW = Math.max(1, (int) (origWidth * scale));
            int targetH = Math.max(1, (int) (origHeight * scale));

            BufferedImage resized = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original, 0, 0, targetW, targetH, null);
            g.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(resized, "png", out);
            return out.toByteArray();

        } catch (Exception e) {
            log.debug("Thumbnail generation error: {}", e.getMessage());
            return new byte[0];
        }
    }

    private String createTextSnippet(byte[] bytes) {
        int length = Math.min(bytes.length, MAX_TEXT_PREVIEW_BYTES);
        return new String(bytes, 0, length, StandardCharsets.UTF_8);
    }
}
