package com.recoveryx.filesystem.service;

import com.recoveryx.common.enumtype.FileSystemType;
import com.recoveryx.common.util.ValidationUtils;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.filesystem.common.FileSystemDetector;
import com.recoveryx.filesystem.fat.ExFatFileSystemParser;
import com.recoveryx.filesystem.fat.FatFileSystemParser;
import com.recoveryx.filesystem.ntfs.NtfsFileSystemParser;
import com.recoveryx.filesystem.partition.PartitionDiscoveryService;
import com.recoveryx.filesystem.partition.PartitionEntry;
import com.recoveryx.storage.model.SectorReadResult;
import com.recoveryx.storage.service.SectorReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Top-level facade service for filesystem detection and recovery indexing.
 */
public final class FileSystemService {

    private static final Logger log = LoggerFactory.getLogger(FileSystemService.class);

    private final SectorReaderService sectorReaderService;
    private final PartitionDiscoveryService partitionDiscoveryService;
    private final FileSystemDetector fileSystemDetector;
    private final FileSystemParserRegistry parserRegistry;

    public FileSystemService(SectorReaderService sectorReaderService) {
        this.sectorReaderService = ValidationUtils.requireNonNull(sectorReaderService, "sectorReaderService");
        this.partitionDiscoveryService = new PartitionDiscoveryService(sectorReaderService);
        this.fileSystemDetector = new FileSystemDetector();

        List<FileSystemParser> parsers = List.of(
                new NtfsFileSystemParser(sectorReaderService),
                new FatFileSystemParser(sectorReaderService),
                new ExFatFileSystemParser(sectorReaderService)
        );
        this.parserRegistry = new FileSystemParserRegistry(parsers);
    }

    public List<PartitionEntry> discoverPartitions(String devicePath, int bytesPerSector, long totalDriveSectors) {
        return partitionDiscoveryService.discoverPartitions(devicePath, bytesPerSector, totalDriveSectors);
    }

    public List<RecoverableFile> scanFilesystem(
            String devicePath,
            long partitionStartSector,
            long partitionSectorCount,
            int bytesPerSector,
            Consumer<RecoverableFile> fileConsumer) {

        ValidationUtils.requireNotBlank(devicePath, "devicePath");
        if (bytesPerSector <= 0) {
            bytesPerSector = 512;
        }

        try {
            // 1. Detect FileSystem
            SectorReadResult bootSectorResult = sectorReaderService.read(devicePath, partitionStartSector, 1, bytesPerSector);
            FileSystemType detectedType = fileSystemDetector.detectFileSystem(bootSectorResult.data());

            log.info("Detected filesystem {} at sector {} on {}", detectedType, partitionStartSector, devicePath);

            Optional<FileSystemParser> parserOpt = parserRegistry.getParser(detectedType);
            if (parserOpt.isPresent()) {
                return parserOpt.get().parseVolume(
                        devicePath,
                        partitionStartSector,
                        partitionSectorCount,
                        bytesPerSector,
                        fileConsumer);
            } else {
                log.warn("No specialized parser registered for filesystem type: {}", detectedType);
            }

        } catch (Exception e) {
            log.error("Failed to scan filesystem on {} at sector {}: {}", devicePath, partitionStartSector, e.getMessage(), e);
        }

        return Collections.emptyList();
    }
}
