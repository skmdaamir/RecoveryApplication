package com.recoveryx.storage.model;

import java.util.Objects;

/**
 * Immutable request for sector-based device reads.
 */
public final class SectorReadRequest {

    private final String devicePath;
    private final long startSector;
    private final int sectorCount;
    private final int bytesPerSector;

    public SectorReadRequest(String devicePath, long startSector, int sectorCount, int bytesPerSector) {
        this.devicePath = Objects.requireNonNull(devicePath, "devicePath must not be null");
        if (startSector < 0) {
            throw new IllegalArgumentException("startSector must be greater than or equal to zero");
        }
        if (sectorCount <= 0) {
            throw new IllegalArgumentException("sectorCount must be greater than zero");
        }
        if (bytesPerSector <= 0) {
            throw new IllegalArgumentException("bytesPerSector must be greater than zero");
        }
        this.startSector = startSector;
        this.sectorCount = sectorCount;
        this.bytesPerSector = bytesPerSector;
    }

    public String devicePath() {
        return devicePath;
    }

    public long startSector() {
        return startSector;
    }

    public int sectorCount() {
        return sectorCount;
    }

    public int bytesPerSector() {
        return bytesPerSector;
    }

    public long startOffsetBytes() {
        return startSector * bytesPerSector;
    }

    public int totalByteCount() {
        return sectorCount * bytesPerSector;
    }

    public String cacheKey() {
        return devicePath + ":" + startSector + ":" + sectorCount + ":" + bytesPerSector;
    }
}