package com.recoveryx.filesystem.fat;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.common.enumtype.FileSystemType;
import com.recoveryx.common.enumtype.HealthStatus;
import com.recoveryx.common.enumtype.RecoveryChance;
import com.recoveryx.common.util.IdGenerator;
import com.recoveryx.common.util.ValidationUtils;
import com.recoveryx.core.domain.file.FileFragment;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.filesystem.service.FileSystemParser;
import com.recoveryx.storage.model.SectorReadResult;
import com.recoveryx.storage.service.SectorReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Parses exFAT file systems and extracts files from directory entry sets.
 */
public final class ExFatFileSystemParser implements FileSystemParser {

    private static final Logger log = LoggerFactory.getLogger(ExFatFileSystemParser.class);

    private final SectorReaderService sectorReaderService;

    public ExFatFileSystemParser(SectorReaderService sectorReaderService) {
        this.sectorReaderService = ValidationUtils.requireNonNull(sectorReaderService, "sectorReaderService");
    }

    @Override
    public FileSystemType getSupportedFileSystemType() {
        return FileSystemType.EXFAT;
    }

    @Override
    public List<RecoverableFile> parseVolume(
            String devicePath,
            long partitionStartSector,
            long partitionSectorCount,
            int bytesPerSector,
            Consumer<RecoverableFile> fileConsumer) {

        ValidationUtils.requireNotBlank(devicePath, "devicePath");
        if (bytesPerSector <= 0) {
            bytesPerSector = 512;
        }

        List<RecoverableFile> results = new ArrayList<>();

        try {
            // 1. Read Boot Sector
            SectorReadResult bootResult = sectorReaderService.read(devicePath, partitionStartSector, 1, bytesPerSector);
            ExFatBootSector bootSector = ExFatBootSector.parse(bootResult.data());

            int clusterSize = bootSector.getClusterSizeBytes();
            int sectorsPerCluster = bootSector.getSectorsPerCluster();
            long rootCluster = bootSector.getRootDirectoryCluster();

            log.info("Parsing exFAT on {}: ClusterSize={}, RootDirCluster={}", devicePath, clusterSize, rootCluster);

            // Read root directory cluster (limit to initial clusters)
            long rootSector = bootSector.clusterToSector(rootCluster, partitionStartSector);
            SectorReadResult dirResult = sectorReaderService.read(devicePath, rootSector, sectorsPerCluster * 4, bytesPerSector);
            byte[] dirData = dirResult.data();

            int totalEntries = dirData.length / 32;
            int i = 0;
            long fileIndex = 0;

            while (i < totalEntries) {
                int offset = i * 32;
                byte entryType = dirData[offset];

                if (entryType == 0x00) {
                    // End of directory
                    break;
                }

                if (ExFatDirectoryEntry.isFileEntry(entryType)) {
                    boolean inUse = (entryType & ExFatDirectoryEntry.IN_USE_MASK) != 0;
                    boolean isDeleted = !inUse;

                    int secondaryCount = dirData[offset + 1] & 0xFF;
                    ByteBuffer buf = ByteBuffer.wrap(dirData, offset, 32).order(ByteOrder.LITTLE_ENDIAN);
                    int attr = buf.getShort(4) & 0xFFFF;
                    boolean isDir = (attr & 0x10) != 0;

                    int createTime = buf.getInt(8);
                    int modTime = buf.getInt(12);

                    // Scan secondary entries
                    long firstCluster = 0;
                    long dataLength = 0;
                    boolean noFatChain = true;
                    StringBuilder nameBuilder = new StringBuilder();

                    for (int s = 1; s <= secondaryCount && (i + s) < totalEntries; s++) {
                        int secOffset = (i + s) * 32;
                        byte secType = dirData[secOffset];

                        if (ExFatDirectoryEntry.isStreamExtension(secType)) {
                            ByteBuffer sBuf = ByteBuffer.wrap(dirData, secOffset, 32).order(ByteOrder.LITTLE_ENDIAN);
                            byte flags = sBuf.get(1);
                            noFatChain = (flags & 0x02) != 0;
                            firstCluster = Integer.toUnsignedLong(sBuf.getInt(20));
                            dataLength = sBuf.getLong(24);
                        } else if (ExFatDirectoryEntry.isFileName(secType)) {
                            byte[] nameEntry = new byte[32];
                            System.arraycopy(dirData, secOffset, nameEntry, 0, 32);
                            nameBuilder.append(ExFatDirectoryEntry.parseFileNamePiece(nameEntry));
                        }
                    }

                    i += (secondaryCount + 1); // Advance past the full entry set

                    String fileName = nameBuilder.toString().trim();
                    if (fileName.isEmpty() || isDir) {
                        continue;
                    }

                    // Build fragments
                    List<FileFragment> fragments = new ArrayList<>();
                    if (firstCluster >= 2 && dataLength > 0) {
                        long clusterCount = (dataLength + clusterSize - 1) / clusterSize;
                        clusterCount = Math.min(10_000, clusterCount);
                        for (int c = 0; c < clusterCount; c++) {
                            long cSector = bootSector.clusterToSector(firstCluster + c, partitionStartSector);
                            long startByte = cSector * bytesPerSector;
                            fragments.add(new FileFragment(startByte, clusterSize, c));
                        }
                    }

                    String ext = extractExtension(fileName);
                    FileCategory category = resolveCategory(ext);

                    HealthStatus health = isDeleted ? HealthStatus.FAIR : HealthStatus.EXCELLENT;
                    RecoveryChance chance = isDeleted ? RecoveryChance.HIGH : RecoveryChance.EXCELLENT;

                    RecoverableFile file = new RecoverableFile(
                            "exfat-" + (fileIndex++) + "-" + IdGenerator.newId(),
                            fileName,
                            ext,
                            category,
                            "/" + fileName,
                            null,
                            dataLength,
                            isDeleted ? Instant.now() : null,
                            Instant.now(),
                            Instant.now(),
                            health,
                            chance,
                            isCategoryPreviewable(category),
                            false,
                            fragments,
                            null);

                    results.add(file);
                    if (fileConsumer != null) {
                        fileConsumer.accept(file);
                    }

                } else {
                    i++;
                }
            }

            log.info("exFAT parse completed on {}: Found {} recoverable files", devicePath, results.size());

        } catch (Exception e) {
            log.warn("Error parsing exFAT volume on {}: {}", devicePath, e.getMessage(), e);
        }

        return Collections.unmodifiableList(results);
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
