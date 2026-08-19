package com.recoveryx.storage.service;

import com.recoveryx.storage.model.SectorReadRequest;
import com.recoveryx.storage.model.SectorReadResult;

import java.util.Objects;

/**
 * High-level service for sector-based reads.
 */
public final class SectorReaderService {

    private final RawSectorReader rawSectorReader;

    public SectorReaderService(RawSectorReader rawSectorReader) {
        this.rawSectorReader = Objects.requireNonNull(
                rawSectorReader,
                "rawSectorReader must not be null");
    }

    public SectorReadResult read(String devicePath, long startSector, int sectorCount, int bytesPerSector) {
        SectorReadRequest request = new SectorReadRequest(
                devicePath,
                startSector,
                sectorCount,
                bytesPerSector);
        return rawSectorReader.read(request);
    }
}