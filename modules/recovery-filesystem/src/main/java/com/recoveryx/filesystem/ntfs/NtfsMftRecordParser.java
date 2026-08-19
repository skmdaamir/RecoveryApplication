package com.recoveryx.filesystem.ntfs;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.common.enumtype.HealthStatus;
import com.recoveryx.common.enumtype.RecoveryChance;
import com.recoveryx.common.util.IdGenerator;
import com.recoveryx.core.domain.file.FileFragment;
import com.recoveryx.core.domain.file.RecoverableFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parses individual 1024-byte MFT records, applies fixups, parses attributes, and extracts RecoverableFile models.
 */
public final class NtfsMftRecordParser {

    private static final Logger log = LoggerFactory.getLogger(NtfsMftRecordParser.class);

    public RecoverableFile parseRecord(
            byte[] recordBytes,
            long recordIndex,
            int clusterSizeBytes,
            long partitionStartOffsetBytes) {

        if (recordBytes == null || recordBytes.length < NtfsConstants.DEFAULT_MFT_RECORD_SIZE) {
            return null;
        }

        byte[] workingCopy = recordBytes.clone();
        if (!applyFixupArray(workingCopy)) {
            log.debug("Fixup array validation failed for MFT record {}", recordIndex);
            // Continue parsing anyway as best effort for data recovery
        }

        ByteBuffer buffer = ByteBuffer.wrap(workingCopy).order(ByteOrder.LITTLE_ENDIAN);

        String magic = new String(workingCopy, 0, 4, StandardCharsets.US_ASCII);
        if (!NtfsConstants.MAGIC_FILE.equals(magic)) {
            return null;
        }

        short flags = buffer.getShort(0x16);
        boolean inUse = (flags & NtfsConstants.FLAG_IN_USE) != 0;
        boolean isDirectory = (flags & NtfsConstants.FLAG_DIRECTORY) != 0;

        int firstAttrOffset = buffer.getShort(0x14) & 0xFFFF;
        int bytesInUse = buffer.getInt(0x18);

        List<NtfsFileNameAttribute> fileNames = new ArrayList<>();
        List<FileFragment> fragments = new ArrayList<>();
        long fileSize = 0;
        boolean hasDataAttribute = false;

        int currentAttrOffset = firstAttrOffset;
        int maxOffset = Math.min(workingCopy.length, bytesInUse > 0 ? bytesInUse : workingCopy.length);

        while (currentAttrOffset >= 0 && currentAttrOffset + 8 <= maxOffset) {
            int attrType = buffer.getInt(currentAttrOffset);
            if (attrType == NtfsConstants.ATTR_END_MARKER || attrType == -1) {
                break;
            }

            int attrLength = buffer.getInt(currentAttrOffset + 4);
            if (attrLength <= 0 || currentAttrOffset + attrLength > workingCopy.length) {
                break;
            }

            byte nonResidentFlag = workingCopy[currentAttrOffset + 8];
            boolean isNonResident = (nonResidentFlag != 0);

            if (!isNonResident) {
                // Resident Attribute
                int valueLength = buffer.getInt(currentAttrOffset + 16);
                int valueOffset = buffer.getShort(currentAttrOffset + 20) & 0xFFFF;
                int absValueOffset = currentAttrOffset + valueOffset;

                if (absValueOffset + valueLength <= workingCopy.length) {
                    if (attrType == NtfsConstants.ATTR_FILE_NAME) {
                        byte[] nameData = new byte[valueLength];
                        System.arraycopy(workingCopy, absValueOffset, nameData, 0, valueLength);
                        NtfsFileNameAttribute fnAttr = NtfsFileNameAttribute.parse(nameData);
                        if (fnAttr != null && !fnAttr.getFileName().isBlank()) {
                            fileNames.add(fnAttr);
                        }
                    } else if (attrType == NtfsConstants.ATTR_DATA) {
                        hasDataAttribute = true;
                        fileSize = valueLength;
                        // Resident data is contained inside the MFT record itself
                        long residentFileOffset = partitionStartOffsetBytes + (recordIndex * workingCopy.length) + absValueOffset;
                        fragments.add(new FileFragment(residentFileOffset, valueLength, 0));
                    }
                }
            } else {
                // Non-Resident Attribute
                if (currentAttrOffset + 64 <= workingCopy.length) {
                    long dataSize = buffer.getLong(currentAttrOffset + 48);
                    int dataRunsOffset = buffer.getShort(currentAttrOffset + 32) & 0xFFFF;

                    if (attrType == NtfsConstants.ATTR_DATA) {
                        hasDataAttribute = true;
                        fileSize = dataSize;
                        int absDataRunsOffset = currentAttrOffset + dataRunsOffset;
                        int maxRunsLength = attrLength - dataRunsOffset;

                        List<FileFragment> decodedFragments = NtfsDataRunDecoder.decode(
                                workingCopy,
                                absDataRunsOffset,
                                maxRunsLength,
                                clusterSizeBytes,
                                partitionStartOffsetBytes);
                        fragments.addAll(decodedFragments);
                    }
                }
            }

            currentAttrOffset += attrLength;
        }

        // Choose best filename (prefer Win32 namespace 1 or 3 over DOS 2)
        NtfsFileNameAttribute selectedName = selectBestFileName(fileNames);
        if (selectedName == null && !hasDataAttribute) {
            return null; // No filename and no data, skip empty record
        }

        String rawName = selectedName != null ? selectedName.getFileName() : "File_" + recordIndex;
        // Skip NTFS system meta-files like $MFT, $LogFile, $Volume unless requested
        if (rawName.startsWith("$") && !rawName.startsWith("$Recycle.Bin") && inUse) {
            return null;
        }

        String ext = extractExtension(rawName);
        FileCategory category = resolveCategory(ext);

        Instant created = selectedName != null ? selectedName.getCreationTime() : Instant.now();
        Instant modified = selectedName != null ? selectedName.getModificationTime() : Instant.now();
        Instant deleted = inUse ? null : modified;

        HealthStatus health = inUse ? HealthStatus.EXCELLENT : (fragments.isEmpty() ? HealthStatus.POOR : HealthStatus.GOOD);
        RecoveryChance chance = inUse ? RecoveryChance.EXCELLENT : (fragments.isEmpty() ? RecoveryChance.LOW : RecoveryChance.HIGH);

        return new RecoverableFile(
                "ntfs-" + recordIndex + "-" + IdGenerator.newId(),
                rawName,
                ext,
                category,
                selectedName != null ? "/" + selectedName.getFileName() : "/" + rawName,
                null,
                fileSize,
                deleted,
                created,
                modified,
                health,
                chance,
                isCategoryPreviewable(category),
                false,
                fragments,
                null);
    }

    private static boolean applyFixupArray(byte[] record) {
        if (record.length < 512) {
            return false;
        }
        int updateSeqOffset = ((record[4] & 0xFF) | ((record[5] & 0xFF) << 8));
        int updateSeqSize = ((record[6] & 0xFF) | ((record[7] & 0xFF) << 8));

        if (updateSeqOffset <= 0 || updateSeqSize < 2 || updateSeqOffset + (updateSeqSize * 2) > record.length) {
            return false;
        }

        int expectedSeqNum = ((record[updateSeqOffset] & 0xFF) | ((record[updateSeqOffset + 1] & 0xFF) << 8));
        int numSectors = updateSeqSize - 1;

        for (int i = 0; i < numSectors; i++) {
            int sectorEndOffset = ((i + 1) * 512) - 2;
            if (sectorEndOffset + 1 >= record.length) {
                break;
            }

            int actualSeqNum = ((record[sectorEndOffset] & 0xFF) | ((record[sectorEndOffset + 1] & 0xFF) << 8));
            if (actualSeqNum != expectedSeqNum) {
                log.trace("Fixup mismatch at sector {}: expected 0x{:04X}, found 0x{:04X}", i, expectedSeqNum, actualSeqNum);
            }

            // Restore original 2 bytes from array
            int arrayEntryOffset = updateSeqOffset + 2 + (i * 2);
            record[sectorEndOffset] = record[arrayEntryOffset];
            record[sectorEndOffset + 1] = record[arrayEntryOffset + 1];
        }

        return true;
    }

    private static NtfsFileNameAttribute selectBestFileName(List<NtfsFileNameAttribute> list) {
        if (list.isEmpty()) {
            return null;
        }
        // Preferred: namespace 1 (Win32) or 3 (Win32 & DOS)
        for (NtfsFileNameAttribute fn : list) {
            if (fn.getNamespace() == 1 || fn.getNamespace() == 3) {
                return fn;
            }
        }
        return list.get(0);
    }

    private static String extractExtension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot > 0 && dot < name.length() - 1) {
            String ext = name.substring(dot + 1).toLowerCase();
            if (!ext.isBlank()) {
                return ext;
            }
        }
        return "dat";
    }

    private static FileCategory resolveCategory(String ext) {
        return switch (ext.toLowerCase()) {
            case "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "tiff" -> FileCategory.IMAGE;
            case "raw", "cr2", "nef", "arw", "dng" -> FileCategory.RAW_IMAGE;
            case "pdf" -> FileCategory.PDF;
            case "doc", "docx", "xls", "xlsx", "ppt", "pptx" -> FileCategory.OFFICE;
            case "txt", "rtf", "odt", "csv", "log", "md" -> FileCategory.DOCUMENT;
            case "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm" -> FileCategory.VIDEO;
            case "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a" -> FileCategory.AUDIO;
            case "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso" -> FileCategory.ARCHIVE;
            case "java", "c", "cpp", "py", "js", "ts", "html", "css", "xml", "json" -> FileCategory.SOURCE_CODE;
            case "exe", "msi", "dll", "bat", "cmd", "ps1" -> FileCategory.EXECUTABLE;
            case "db", "sqlite", "sql", "mdb", "accdb" -> FileCategory.DATABASE;
            default -> FileCategory.UNKNOWN;
        };
    }

    private static boolean isCategoryPreviewable(FileCategory cat) {
        return cat == FileCategory.IMAGE || cat == FileCategory.PDF || cat == FileCategory.DOCUMENT || cat == FileCategory.AUDIO || cat == FileCategory.SOURCE_CODE;
    }
}
