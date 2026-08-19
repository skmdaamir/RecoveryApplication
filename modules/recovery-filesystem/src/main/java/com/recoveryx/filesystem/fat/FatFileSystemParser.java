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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Parses FAT32 file systems, traverses directory cluster chains, and recovers active and deleted files.
 */
public final class FatFileSystemParser implements FileSystemParser {

    private static final Logger log = LoggerFactory.getLogger(FatFileSystemParser.class);

    private final SectorReaderService sectorReaderService;

    public FatFileSystemParser(SectorReaderService sectorReaderService) {
        this.sectorReaderService = ValidationUtils.requireNonNull(sectorReaderService, "sectorReaderService");
    }

    @Override
    public FileSystemType getSupportedFileSystemType() {
        return FileSystemType.FAT32;
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
            SectorReadResult bootSectorResult = sectorReaderService.read(devicePath, partitionStartSector, 1, bytesPerSector);
            Fat32BootSector bootSector = Fat32BootSector.parse(bootSectorResult.data());

            int clusterSize = bootSector.getClusterSizeBytes();
            int sectorsPerCluster = bootSector.getSectorsPerCluster();
            long partitionStartOffsetBytes = partitionStartSector * bytesPerSector;

            // 2. Read first FAT table
            long fatStartSector = bootSector.getFatStartSector(partitionStartSector);
            int fatSectorsToRead = (int) Math.min(128, bootSector.getSectorsPerFat());
            SectorReadResult fatResult = sectorReaderService.read(devicePath, fatStartSector, fatSectorsToRead, bytesPerSector);
            Fat32Table fatTable = new Fat32Table(fatResult.data());

            log.info("Parsing FAT32 on {}: ClusterSize={}, RootDirCluster={}",
                    devicePath, clusterSize, bootSector.getRootDirectoryCluster());

            // 3. Queue directory clusters to traverse
            Queue<Long> dirClusters = new LinkedList<>();
            dirClusters.add(bootSector.getRootDirectoryCluster());
            int visitedDirs = 0;
            long fileIndex = 0;

            while (!dirClusters.isEmpty() && visitedDirs < 1000) {
                long currentDirCluster = dirClusters.poll();
                visitedDirs++;

                List<Long> dirClusterChain = fatTable.getClusterChain(currentDirCluster, 100);
                if (dirClusterChain.isEmpty()) {
                    dirClusterChain = List.of(currentDirCluster);
                }

                StringBuilder lfnBuilder = new StringBuilder();

                for (long cluster : dirClusterChain) {
                    long clusterSector = bootSector.clusterToSector(cluster, partitionStartSector);
                    SectorReadResult clusterResult = sectorReaderService.read(devicePath, clusterSector, sectorsPerCluster, bytesPerSector);
                    byte[] clusterData = clusterResult.data();

                    int entryCount = clusterData.length / Fat32DirectoryEntry.ENTRY_SIZE;

                    for (int e = 0; e < entryCount; e++) {
                        int offset = e * Fat32DirectoryEntry.ENTRY_SIZE;
                        byte firstByte = clusterData[offset];

                        if (firstByte == Fat32DirectoryEntry.END_OF_DIR_MARKER) {
                            break;
                        }

                        byte attr = clusterData[offset + 11];

                        if (attr == Fat32DirectoryEntry.ATTR_LFN) {
                            byte[] lfnEntry = new byte[Fat32DirectoryEntry.ENTRY_SIZE];
                            System.arraycopy(clusterData, offset, lfnEntry, 0, Fat32DirectoryEntry.ENTRY_SIZE);
                            String piece = Fat32DirectoryEntry.parseLfnPiece(lfnEntry);
                            lfnBuilder.insert(0, piece);
                            continue;
                        }

                        byte[] dirEntryBytes = new byte[Fat32DirectoryEntry.ENTRY_SIZE];
                        System.arraycopy(clusterData, offset, dirEntryBytes, 0, Fat32DirectoryEntry.ENTRY_SIZE);

                        String currentLfn = lfnBuilder.toString();
                        lfnBuilder.setLength(0); // Reset accumulator

                        Fat32DirectoryEntry entry = Fat32DirectoryEntry.parse(dirEntryBytes, currentLfn);
                        if (entry == null || entry.isVolumeId() || ".".equals(entry.getShortName()) || "..".equals(entry.getShortName())) {
                            continue;
                        }

                        if (entry.isDirectory()) {
                            if (entry.getStartingCluster() >= 2 && !entry.isDeleted()) {
                                dirClusters.add(entry.getStartingCluster());
                            }
                            continue;
                        }

                        // Reconstruct fragments from FAT chain or contiguous assumption if deleted
                        List<FileFragment> fragments = new ArrayList<>();
                        long startCluster = entry.getStartingCluster();

                        if (startCluster >= 2) {
                            if (!entry.isDeleted()) {
                                List<Long> fileClusters = fatTable.getClusterChain(startCluster, 10_000);
                                int seq = 0;
                                for (long fc : fileClusters) {
                                    long fcSector = bootSector.clusterToSector(fc, partitionStartSector);
                                    long startByte = fcSector * bytesPerSector;
                                    fragments.add(new FileFragment(startByte, clusterSize, seq++));
                                }
                            } else {
                                long neededClusters = Math.max(1, (entry.getFileSize() + clusterSize - 1) / clusterSize);
                                neededClusters = Math.min(10_000, neededClusters);
                                for (int i = 0; i < neededClusters; i++) {
                                    long fcSector = bootSector.clusterToSector(startCluster + i, partitionStartSector);
                                    long startByte = fcSector * bytesPerSector;
                                    fragments.add(new FileFragment(startByte, clusterSize, i));
                                }
                            }
                        }

                        String fileName = entry.getLongName();
                        String ext = extractExtension(fileName);
                        FileCategory category = resolveCategory(ext);

                        HealthStatus health = entry.isDeleted() ? HealthStatus.FAIR : HealthStatus.EXCELLENT;
                        RecoveryChance chance = entry.isDeleted() ? RecoveryChance.HIGH : RecoveryChance.EXCELLENT;

                        RecoverableFile file = new RecoverableFile(
                                "fat32-" + (fileIndex++) + "-" + IdGenerator.newId(),
                                fileName,
                                ext,
                                category,
                                "/" + fileName,
                                null,
                                entry.getFileSize(),
                                entry.isDeleted() ? entry.getModificationTime() : null,
                                entry.getCreationTime(),
                                entry.getModificationTime(),
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
                    }
                }
            }

            log.info("FAT32 parse completed on {}: Found {} recoverable files", devicePath, results.size());

        } catch (Exception e) {
            log.warn("Error parsing FAT32 volume on {}: {}", devicePath, e.getMessage(), e);
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
