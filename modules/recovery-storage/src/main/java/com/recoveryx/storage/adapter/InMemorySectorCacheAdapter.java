package com.recoveryx.storage.adapter;

import com.recoveryx.core.port.storage.SectorCachePort;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory sector cache for aligned read blocks.
 */
@Component
public class InMemorySectorCacheAdapter implements SectorCachePort {

    private final Map<String, byte[]> cache = new ConcurrentHashMap<>();

    @Override
    public Optional<byte[]> get(String devicePath, long offsetBytes, int lengthBytes) {
        byte[] data = cache.get(key(devicePath, offsetBytes, lengthBytes));
        return data == null ? Optional.empty() : Optional.of(Arrays.copyOf(data, data.length));
    }

    @Override
    public void put(String devicePath, long offsetBytes, int lengthBytes, byte[] data) {
        cache.put(key(devicePath, offsetBytes, lengthBytes), Arrays.copyOf(data, data.length));
    }

    @Override
    public void clearDevice(String devicePath) {
        cache.keySet().removeIf(key -> key.startsWith(devicePath + "|"));
    }

    @Override
    public void clearAll() {
        cache.clear();
    }

    private String key(String devicePath, long offsetBytes, int lengthBytes) {
        return devicePath + "|" + offsetBytes + "|" + lengthBytes;
    }
}