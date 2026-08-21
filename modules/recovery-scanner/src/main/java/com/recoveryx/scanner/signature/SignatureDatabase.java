package com.recoveryx.scanner.signature;

import com.recoveryx.common.enumtype.FileCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Built-in registry and database of magic file signatures for image and media carving.
 */
public final class SignatureDatabase {

    private final List<FileSignature> signatures;

    public SignatureDatabase() {
        this.signatures = initDefaultSignatures();
    }

    public List<FileSignature> getAllSignatures() {
        return Collections.unmodifiableList(signatures);
    }

    public List<FileSignature> match(byte[] sectorData, int offset) {
        if (sectorData == null || offset < 0 || offset >= sectorData.length) {
            return Collections.emptyList();
        }
        List<FileSignature> matches = new ArrayList<>();
        for (FileSignature sig : signatures) {
            if (sig.matchesHeader(sectorData, offset)) {
                matches.add(sig);
            }
        }
        return matches;
    }

    private static List<FileSignature> initDefaultSignatures() {
        List<FileSignature> list = new ArrayList<>();

        // 1. Images (Photos)
        // JPEG / JPG
        list.add(new FileSignature(
                "jpg",
                FileCategory.IMAGE,
                "JPEG / JPG Image",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
                0,
                new byte[]{(byte) 0xFF, (byte) 0xD9},
                50 * 1024 * 1024L, // 50 MB max
                98));

        // PNG Image
        list.add(new FileSignature(
                "png",
                FileCategory.IMAGE,
                "PNG Image",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A},
                0,
                new byte[]{0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82}, // IEND chunk
                100 * 1024 * 1024L,
                100));

        // GIF Image (GIF89a / GIF87a)
        list.add(new FileSignature(
                "gif",
                FileCategory.IMAGE,
                "GIF Image (89a)",
                new byte[]{0x47, 0x49, 0x46, 0x38, 0x39, 0x61},
                0,
                new byte[]{0x00, 0x3B},
                50 * 1024 * 1024L,
                95));

        list.add(new FileSignature(
                "gif",
                FileCategory.IMAGE,
                "GIF Image (87a)",
                new byte[]{0x47, 0x49, 0x46, 0x38, 0x37, 0x61},
                0,
                new byte[]{0x00, 0x3B},
                50 * 1024 * 1024L,
                95));

        // BMP Image
        list.add(new FileSignature(
                "bmp",
                FileCategory.IMAGE,
                "BMP Image",
                new byte[]{0x42, 0x4D},
                0,
                null,
                100 * 1024 * 1024L,
                85));

        // TIFF Image (Little Endian)
        list.add(new FileSignature(
                "tiff",
                FileCategory.IMAGE,
                "TIFF Image (LE)",
                new byte[]{0x49, 0x49, 0x2A, 0x00},
                0,
                null,
                200 * 1024 * 1024L,
                90));

        // TIFF Image (Big Endian)
        list.add(new FileSignature(
                "tiff",
                FileCategory.IMAGE,
                "TIFF Image (BE)",
                new byte[]{0x4D, 0x4D, 0x00, 0x2A},
                0,
                null,
                200 * 1024 * 1024L,
                90));

        // Canon Camera RAW (.CR2)
        list.add(new FileSignature(
                "cr2",
                FileCategory.RAW_IMAGE,
                "Canon RAW Image (.CR2)",
                new byte[]{0x49, 0x49, 0x2A, 0x00, 0x10, 0x00, 0x00, 0x00, 0x43, 0x52},
                0,
                null,
                150 * 1024 * 1024L,
                99));

        // Canon Camera RAW v3 (.CR3)
        list.add(new FileSignature(
                "cr3",
                FileCategory.RAW_IMAGE,
                "Canon RAW Image (.CR3)",
                new byte[]{0x63, 0x72, 0x78, 0x20}, // crx 
                8, // offset 8
                null,
                150 * 1024 * 1024L,
                98));

        // Nikon Camera RAW (.NEF)
        list.add(new FileSignature(
                "nef",
                FileCategory.RAW_IMAGE,
                "Nikon RAW Image (.NEF)",
                new byte[]{0x49, 0x49, 0x2A, 0x00, (byte) 0x1C, 0x00, 0x00, 0x00},
                0,
                null,
                150 * 1024 * 1024L,
                99));

        // Sony Camera RAW (.ARW)
        list.add(new FileSignature(
                "arw",
                FileCategory.RAW_IMAGE,
                "Sony RAW Image (.ARW)",
                new byte[]{0x49, 0x49, 0x2A, 0x00},
                0,
                null,
                150 * 1024 * 1024L,
                92));

        // Adobe Digital Negative (.DNG)
        list.add(new FileSignature(
                "dng",
                FileCategory.RAW_IMAGE,
                "Adobe RAW Digital Negative (.DNG)",
                new byte[]{0x49, 0x49, 0x2A, 0x00},
                0,
                null,
                150 * 1024 * 1024L,
                90));

        // Fujifilm Camera RAW (.RAF)
        list.add(new FileSignature(
                "raf",
                FileCategory.RAW_IMAGE,
                "Fujifilm RAW Image (.RAF)",
                new byte[]{0x46, 0x55, 0x4A, 0x49, 0x46, 0x49, 0x4C, 0x4D}, // FUJIFILM
                0,
                null,
                150 * 1024 * 1024L,
                99));

        // Olympus Camera RAW (.ORF)
        list.add(new FileSignature(
                "orf",
                FileCategory.RAW_IMAGE,
                "Olympus RAW Image (.ORF)",
                new byte[]{0x49, 0x49, 0x52, 0x4F}, // IIRO
                0,
                null,
                150 * 1024 * 1024L,
                99));

        // Panasonic Camera RAW (.RW2)
        list.add(new FileSignature(
                "rw2",
                FileCategory.RAW_IMAGE,
                "Panasonic RAW Image (.RW2)",
                new byte[]{0x49, 0x49, 0x55, 0x00}, // IIU.
                0,
                null,
                150 * 1024 * 1024L,
                99));

        // Google WebP Image (.webp)
        list.add(new FileSignature(
                "webp",
                FileCategory.IMAGE,
                "WebP Image",
                new byte[]{0x57, 0x45, 0x42, 0x50}, // WEBP
                8, // offset 8
                null,
                50 * 1024 * 1024L,
                95));

        // Apple / Modern Phone HEIC Image (.heic)
        list.add(new FileSignature(
                "heic",
                FileCategory.IMAGE,
                "High Efficiency Image (.HEIC)",
                new byte[]{0x68, 0x65, 0x69, 0x63}, // heic
                8, // offset 8
                null,
                50 * 1024 * 1024L,
                95));

        // 2. Documents
        // PDF Document
        list.add(new FileSignature(
                "pdf",
                FileCategory.PDF,
                "Adobe PDF Document",
                new byte[]{0x25, 0x50, 0x44, 0x46}, // %PDF
                0,
                new byte[]{0x25, 0x25, 0x45, 0x4F, 0x46}, // %%EOF
                200 * 1024 * 1024L,
                95));

        // ZIP / Office Documents (.docx, .xlsx, .pptx, .zip)
        list.add(new FileSignature(
                "zip",
                FileCategory.ARCHIVE,
                "ZIP / Office OpenXML Archive",
                new byte[]{0x50, 0x4B, 0x03, 0x04}, // PK..
                0,
                new byte[]{0x50, 0x4B, 0x05, 0x06}, // End of Central Directory
                1024 * 1024 * 1024L, // 1 GB
                90));

        // 3. Audio & Video
        // MP4 / MOV Video
        list.add(new FileSignature(
                "mp4",
                FileCategory.VIDEO,
                "MP4 / MOV Video",
                new byte[]{0x66, 0x74, 0x79, 0x70}, // ftyp
                4, // Offset 4
                null,
                4L * 1024 * 1024 * 1024L, // 4 GB
                92));

        // MP3 Audio (ID3 tag)
        list.add(new FileSignature(
                "mp3",
                FileCategory.AUDIO,
                "MP3 Audio (ID3)",
                new byte[]{0x49, 0x44, 0x33}, // ID3
                0,
                null,
                100 * 1024 * 1024L,
                90));

        // WAV Audio (RIFF...WAVE)
        list.add(new FileSignature(
                "wav",
                FileCategory.AUDIO,
                "WAV Audio",
                new byte[]{0x52, 0x49, 0x46, 0x46}, // RIFF
                0,
                null,
                500 * 1024 * 1024L,
                88));

        return list;
    }
}
