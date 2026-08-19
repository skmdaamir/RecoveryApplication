package com.recoveryx.nativeaccess.model;

/**
 * Native drive geometry returned from Windows.
 *
 * @param bytesPerSector bytes per sector
 * @param sectorsPerTrack sectors per track
 * @param tracksPerCylinder tracks per cylinder
 * @param totalBytes total size in bytes
 */
public record NativeDriveGeometry(
        int bytesPerSector,
        long sectorsPerTrack,
        long tracksPerCylinder,
        long totalBytes) {
}