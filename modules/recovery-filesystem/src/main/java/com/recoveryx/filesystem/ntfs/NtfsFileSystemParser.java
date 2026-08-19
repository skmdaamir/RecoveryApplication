package com.recoveryx.filesystem.ntfs;

import com.recoveryx.common.enumtype.FileSystemType;
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
import java.util.List;
import java.util.function.Consumer;

/**
 * High-performance NTFS filesystem parser capable of traversing $MFT records and recovering deleted files.
 */
public final class NtfsFileSystemParser implements FileSystemParser {

    private static final Logger log = LoggerFactory.getLogger(NtfsFileSystemParser.class);

    private final SectorReaderService sectorReaderService;
    private final NtfsMftRecordParser mftRecordParser;

    public NtfsFileSystemParser(SectorReaderService sectorReaderService) {
        this.sectorReaderService = ValidationUtils.requireNonNull(sectorReaderService, "sectorReaderService");
        this.mftRecordParser = new NtfsMftRecordParser();
    }

    @Override
    public FileSystemType getSupportedFileSystemType() {
        return FileSystemType.NTFS;
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
            NtfsBootSector bootSector = NtfsBootSector.parse(bootSectorResult.data());

            int clusterSize = bootSector.getClusterSizeBytes();
            int mftRecordSize = bootSector.getMftRecordSizeBytes();
            long partitionStartOffsetBytes = partitionStartSector * bytesPerSector;
            long mftStartSector = partitionStartSector + (bootSector.getMftClusterLba() * bootSector.getSectorsPerCluster());

            log.info("Parsing NTFS on {}: ClusterSize={}, MftStartSector={}, MftRecordSize={}",
                    devicePath, clusterSize, mftStartSector, mftRecordSize);

            int sectorsPerMftRecord = Math.max(1, mftRecordSize / bytesPerSector);
            int recordsPerBatch = 64; // Read 64 MFT records (64KB) per I/O call
            int sectorsPerBatch = recordsPerBatch * sectorsPerMftRecord;

            // 2. Read first MFT batch to get Record 0 ($MFT) and find total MFT extents
            SectorReadResult firstBatch = sectorReaderService.read(devicePath, mftStartSector, sectorsPerBatch, bytesPerSector);
            byte[] firstBatchData = firstBatch.data();

            // First pass: parse initial MFT records up to a safe limit or iterate whole partition
            long maxRecordsToScan = Math.min(100_000, (partitionSectorCount * bytesPerSector) / mftRecordSize);
            long currentRecordIndex = 0;

            for (long sector = mftStartSector; sector < mftStartSector + (maxRecordsToScan * sectorsPerMftRecord); sector += sectorsPerBatch) {
                SectorReadResult batchResult = (sector == mftStartSector)
                        ? firstBatch
                        : sectorReaderService.read(devicePath, sector, sectorsPerBatch, bytesPerSector);

                byte[] batchData = batchResult.data();
                int batchRecordCount = batchData.length / mftRecordSize;

                if (batchRecordCount == 0) {
                    break;
                }

                boolean encounteredValidRecord = false;

                for (int r = 0; r < batchRecordCount; r++) {
                    long recordIndex = currentRecordIndex++;
                    int recordOffset = r * mftRecordSize;
                    byte[] singleRecord = new byte[mftRecordSize];
                    System.arraycopy(batchData, recordOffset, singleRecord, 0, mftRecordSize);

                    RecoverableFile file = mftRecordParser.parseRecord(
                            singleRecord,
                            recordIndex,
                            clusterSize,
                            partitionStartOffsetBytes);

                    if (file != null) {
                        encounteredValidRecord = true;
                        results.add(file);
                        if (fileConsumer != null) {
                            fileConsumer.accept(file);
                        }
                    }
                }

                // If a full batch of 64 records has no valid FILE signatures at all, we might have reached the end of initialized MFT
                if (!encounteredValidRecord && currentRecordIndex > 1000) {
                    log.debug("Reached end of active MFT records at index {}", currentRecordIndex);
                    break;
                }
            }

            log.info("NTFS parse completed on {}: Found {} recoverable files", devicePath, results.size());

        } catch (Exception e) {
            log.warn("Error parsing NTFS volume on {}: {}", devicePath, e.getMessage(), e);
        }

        return Collections.unmodifiableList(results);
    }
}
