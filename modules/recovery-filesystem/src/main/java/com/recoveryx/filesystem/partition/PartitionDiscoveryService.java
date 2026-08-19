package com.recoveryx.filesystem.partition;

import com.recoveryx.common.enumtype.FileSystemType;
import com.recoveryx.common.util.ValidationUtils;
import com.recoveryx.filesystem.common.FileSystemDetector;
import com.recoveryx.storage.model.SectorReadResult;
import com.recoveryx.storage.service.SectorReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service responsible for analyzing raw drive sectors to discover partition layout (MBR, GPT, or direct Volume).
 */
public final class PartitionDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(PartitionDiscoveryService.class);

    private final SectorReaderService sectorReaderService;
    private final MbrPartitionTableParser mbrParser;
    private final GptPartitionTableParser gptParser;
    private final FileSystemDetector fileSystemDetector;

    public PartitionDiscoveryService(SectorReaderService sectorReaderService) {
        this.sectorReaderService = ValidationUtils.requireNonNull(sectorReaderService, "sectorReaderService");
        this.mbrParser = new MbrPartitionTableParser();
        this.gptParser = new GptPartitionTableParser();
        this.fileSystemDetector = new FileSystemDetector();
    }

    public List<PartitionEntry> discoverPartitions(String devicePath, int bytesPerSector, long totalDriveSectors) {
        ValidationUtils.requireNotBlank(devicePath, "devicePath");
        if (bytesPerSector <= 0) {
            bytesPerSector = 512;
        }

        try {
            // Read LBA 0 (Sector 0)
            SectorReadResult lba0Result = sectorReaderService.read(devicePath, 0, 1, bytesPerSector);
            byte[] lba0 = lba0Result.data();

            // 1. Check if LBA 0 is a direct filesystem boot sector (e.g. partition or floppy/flash direct image)
            FileSystemType directFsType = fileSystemDetector.detectFileSystem(lba0);
            if (directFsType != FileSystemType.UNKNOWN) {
                log.info("Device {} contains direct filesystem boot sector: {}", devicePath, directFsType);
                return List.of(new PartitionEntry(
                        0,
                        0,
                        totalDriveSectors > 0 ? totalDriveSectors : 1,
                        bytesPerSector,
                        PartitionType.MBR_NTFS,
                        directFsType,
                        true,
                        "Direct Volume (" + directFsType + ")"
                ));
            }

            // 2. Check for GPT (Protective MBR on LBA 0 + GPT Header on LBA 1)
            if (mbrParser.isGptProtectiveMbr(lba0)) {
                log.info("Device {} has protective MBR, checking GPT header at LBA 1", devicePath);
                SectorReadResult lba1Result = sectorReaderService.read(devicePath, 1, 1, bytesPerSector);
                if (gptParser.isValidGptHeader(lba1Result.data())) {
                    // Read 32 sectors of partition entries (LBA 2 to 33 = 128 entries * 128 bytes = 16384 bytes)
                    SectorReadResult gptEntriesResult = sectorReaderService.read(devicePath, 2, 32, bytesPerSector);
                    List<PartitionEntry> gptPartitions = gptParser.parseEntries(gptEntriesResult.data(), GptPartitionTableParser.DEFAULT_ENTRY_SIZE, bytesPerSector);
                    if (!gptPartitions.isEmpty()) {
                        log.info("Discovered {} GPT partitions on {}", gptPartitions.size(), devicePath);
                        return gptPartitions;
                    }
                }
            }

            // 3. Fallback to standard MBR
            if (mbrParser.isValidMbr(lba0)) {
                List<PartitionEntry> mbrPartitions = mbrParser.parse(lba0, bytesPerSector);
                if (!mbrPartitions.isEmpty()) {
                    log.info("Discovered {} MBR partitions on {}", mbrPartitions.size(), devicePath);
                    return mbrPartitions;
                }
            }

        } catch (Exception e) {
            log.warn("Failed to read/discover partitions on {}: {}", devicePath, e.getMessage());
        }

        return Collections.emptyList();
    }
}
