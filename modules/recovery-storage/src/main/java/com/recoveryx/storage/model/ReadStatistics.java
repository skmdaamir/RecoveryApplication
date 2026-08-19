package com.recoveryx.storage.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe read metrics for storage operations.
 */
public final class ReadStatistics {

    private final AtomicLong readRequests = new AtomicLong();
    private final AtomicLong cacheHits = new AtomicLong();
    private final AtomicLong bytesRead = new AtomicLong();

    public void incrementReadRequests() {
        readRequests.incrementAndGet();
    }

    public void incrementCacheHits() {
        cacheHits.incrementAndGet();
    }

    public void addBytesRead(long byteCount) {
        bytesRead.addAndGet(byteCount);
    }

    public long readRequests() {
        return readRequests.get();
    }

    public long cacheHits() {
        return cacheHits.get();
    }

    public long bytesRead() {
        return bytesRead.get();
    }
}