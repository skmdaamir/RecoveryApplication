package com.recoveryx.storage.service.impl;

import com.recoveryx.storage.model.SectorReadResult;
import com.recoveryx.storage.service.SectorCache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Bounded LRU cache for sector reads.
 */
public final class DefaultSectorCache implements SectorCache {

    private final int maxEntries;
    private final Map<String, SectorReadResult> cache;

    public DefaultSectorCache(int maxEntries) {
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("maxEntries must be greater than zero");
        }
        this.maxEntries = maxEntries;
        this.cache = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, SectorReadResult> eldest) {
                return size() > DefaultSectorCache.this.maxEntries;
            }
        };
    }

    @Override
    public synchronized Optional<SectorReadResult> get(String key) {
        Objects.requireNonNull(key, "key must not be null");
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public synchronized void put(String key, SectorReadResult value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");
        cache.put(key, value);
    }

    @Override
    public synchronized void clear() {
        cache.clear();
    }

    @Override
    public synchronized int size() {
        return cache.size();
    }
}