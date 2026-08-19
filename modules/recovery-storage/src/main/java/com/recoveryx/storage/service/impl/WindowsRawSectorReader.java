package com.recoveryx.storage.service.impl;

import com.recoveryx.nativeaccess.model.NativeDeviceHandle;
import com.recoveryx.nativeaccess.model.RawReadRequest;
import com.recoveryx.nativeaccess.service.WindowsNativeDiskAccessService;
import com.recoveryx.storage.exception.DeviceAccessException;
import com.recoveryx.storage.exception.InvalidReadRequestException;
import com.recoveryx.storage.model.SectorReadRequest;
import com.recoveryx.storage.model.SectorReadResult;
import com.recoveryx.storage.service.RawSectorReader;
import com.recoveryx.storage.service.SectorCache;

import java.time.Instant;
import java.util.Objects;

/**
 * Windows raw sector reader backed by the native disk access service.
 */
public final class WindowsRawSectorReader implements RawSectorReader {

    private final WindowsNativeDiskAccessService nativeDiskAccessService;
    private final SectorCache sectorCache;

    public WindowsRawSectorReader(
            WindowsNativeDiskAccessService nativeDiskAccessService,
            SectorCache sectorCache) {
        this.nativeDiskAccessService = Objects.requireNonNull(
                nativeDiskAccessService,
                "nativeDiskAccessService must not be null");
        this.sectorCache = Objects.requireNonNull(
                sectorCache,
                "sectorCache must not be null");
    }

    @Override
    public SectorReadResult read(SectorReadRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        validateRequest(request);

        return sectorCache.get(request.cacheKey())
                .orElseGet(() -> loadAndCache(request));
    }

    private void validateRequest(SectorReadRequest request) {
        if (request.startSector() < 0) {
            throw new InvalidReadRequestException("startSector must be greater than or equal to zero");
        }
        if (request.sectorCount() <= 0) {
            throw new InvalidReadRequestException("sectorCount must be greater than zero");
        }
        if (request.bytesPerSector() <= 0) {
            throw new InvalidReadRequestException("bytesPerSector must be greater than zero");
        }
    }

    private SectorReadResult loadAndCache(SectorReadRequest request) {
        NativeDeviceHandle handle = null;
        try {
            long offset = request.startOffsetBytes();
            int totalBytes = request.totalByteCount();

            handle = nativeDiskAccessService.openReadOnly(request.devicePath());
            byte[] data = nativeDiskAccessService.read(handle, new RawReadRequest(offset, totalBytes));

            if (data == null) {
                throw new DeviceAccessException(
                        "Native disk access returned null data for device " + request.devicePath());
            }

            if (data.length != totalBytes) {
                throw new DeviceAccessException(
                        "Unexpected byte count read. Expected " + totalBytes + " but got " + data.length);
            }

            SectorReadResult result = new SectorReadResult(request, data, Instant.now());
            sectorCache.put(request.cacheKey(), result);
            return result;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DeviceAccessException(
                    "Failed to read sectors from device " + request.devicePath(), ex);
        } finally {
            if (handle != null) {
                nativeDiskAccessService.close(handle);
            }
        }
    }
}