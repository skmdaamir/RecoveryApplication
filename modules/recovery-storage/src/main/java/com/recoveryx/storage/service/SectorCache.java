package com.recoveryx.storage.service;

import com.recoveryx.storage.model.SectorReadResult;

import java.util.Optional;

/**
 * Cache for sector read results.
 */
public interface SectorCache {

    Optional<SectorReadResult> get(String key);

    void put(String key, SectorReadResult value);

    void clear();

    int size();
}