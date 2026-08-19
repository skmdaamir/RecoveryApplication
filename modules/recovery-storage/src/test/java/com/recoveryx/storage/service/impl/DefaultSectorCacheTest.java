package com.recoveryx.storage.service.impl;


import com.recoveryx.storage.model.SectorReadRequest;
import com.recoveryx.storage.model.SectorReadResult;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class DefaultSectorCacheTest {

    @Test
    void shouldEvictLeastRecentlyUsedEntry() {
        DefaultSectorCache cache = new DefaultSectorCache(2);
        SectorReadResult first = new SectorReadResult(new SectorReadRequest("\\\\.\\PhysicalDrive0", 0, 1, 512),
                new byte[512], Instant.now());
        SectorReadResult second = new SectorReadResult(new SectorReadRequest("\\\\.\\PhysicalDrive0", 1, 1, 512),
                new byte[512], Instant.now());
        SectorReadResult third = new SectorReadResult(new SectorReadRequest("\\\\.\\PhysicalDrive0", 2, 1, 512),
                new byte[512], Instant.now());
        cache.put("1", first);
        cache.put("2", second);
        cache.get("1");
        cache.put("3", third);

        assertTrue(cache.get("1").isPresent());
        assertTrue(cache.get("2").isEmpty());
        assertTrue(cache.get("3").isPresent());
        assertEquals(2, cache.size());

    }
}
