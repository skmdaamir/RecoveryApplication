package com.recoveryx.storage.adapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemorySectorCacheAdapterTest {

    @Test
    void shouldStoreAndReturnCopies() {
        InMemorySectorCacheAdapter cache = new InMemorySectorCacheAdapter();
        byte[] data = new byte[] { 1, 2, 3 };

        cache.put("\\\\.\\PhysicalDrive0", 0, 3, data);
        data[0] = 9;

        byte[] cached = cache.get("\\\\.\\PhysicalDrive0", 0, 3).orElseThrow();
        assertArrayEquals(new byte[] { 1, 2, 3 }, cached);

        cached[1] = 8;
        byte[] cachedAgain = cache.get("\\\\.\\PhysicalDrive0", 0, 3).orElseThrow();
        assertArrayEquals(new byte[] { 1, 2, 3 }, cachedAgain);
    }
}