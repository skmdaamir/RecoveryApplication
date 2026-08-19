package com.recoveryx.core.port.storage;

import java.util.Optional;

/**
 * Cache abstraction for sector-aligned read blocks.
 */
public interface SectorCachePort {

    Optional<byte[]> get(String devicePath, long offsetBytes, int lengthBytes);

    void put(String devicePath, long offsetBytes, int lengthBytes, byte[] data);

    void clearDevice(String devicePath);

    void clearAll();
}