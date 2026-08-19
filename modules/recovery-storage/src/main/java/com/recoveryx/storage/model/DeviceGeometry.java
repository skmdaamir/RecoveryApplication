package com.recoveryx.storage.model;

import java.util.Objects;

/**
 * Immutable device geometry information.
 */
public final class DeviceGeometry {

    private final long cylinders;
    private final int tracksPerCylinder;
    private final int sectorsPerTrack;
    private final int bytesPerSector;
    private final long totalBytes;

    public DeviceGeometry(long cylinders,
            int tracksPerCylinder,
            int sectorsPerTrack,
            int bytesPerSector,
            long totalBytes) {
        if (cylinders < 0) {
            throw new IllegalArgumentException("cylinders must be greater than or equal to zero");
        }
        if (tracksPerCylinder <= 0) {
            throw new IllegalArgumentException("tracksPerCylinder must be greater than zero");
        }
        if (sectorsPerTrack <= 0) {
            throw new IllegalArgumentException("sectorsPerTrack must be greater than zero");
        }
        if (bytesPerSector <= 0) {
            throw new IllegalArgumentException("bytesPerSector must be greater than zero");
        }
        if (totalBytes < 0) {
            throw new IllegalArgumentException("totalBytes must be greater than or equal to zero");
        }
        this.cylinders = cylinders;
        this.tracksPerCylinder = tracksPerCylinder;
        this.sectorsPerTrack = sectorsPerTrack;
        this.bytesPerSector = bytesPerSector;
        this.totalBytes = totalBytes;
    }

    public long cylinders() {
        return cylinders;
    }

    public int tracksPerCylinder() {
        return tracksPerCylinder;
    }

    public int sectorsPerTrack() {
        return sectorsPerTrack;
    }

    public int bytesPerSector() {
        return bytesPerSector;
    }

    public long totalBytes() {
        return totalBytes;
    }

    public long totalSectors() {
        return bytesPerSector == 0 ? 0 : totalBytes / bytesPerSector;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DeviceGeometry that)) {
            return false;
        }
        return cylinders == that.cylinders
                && tracksPerCylinder == that.tracksPerCylinder
                && sectorsPerTrack == that.sectorsPerTrack
                && bytesPerSector == that.bytesPerSector
                && totalBytes == that.totalBytes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cylinders, tracksPerCylinder, sectorsPerTrack, bytesPerSector, totalBytes);
    }
}