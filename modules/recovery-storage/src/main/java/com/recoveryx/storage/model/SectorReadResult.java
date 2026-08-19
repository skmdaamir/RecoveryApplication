package com.recoveryx.storage.model;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * Result of a sector read request.
 */
public final class SectorReadResult {

    private final SectorReadRequest request;
    private final byte[] data;
    private final Instant readTime;

    public SectorReadResult(SectorReadRequest request, byte[] data, Instant readTime) {
        this.request = Objects.requireNonNull(request, "request must not be null");
        this.data = Objects.requireNonNull(data, "data must not be null").clone();
        this.readTime = Objects.requireNonNull(readTime, "readTime must not be null");
    }

    public SectorReadRequest request() {
        return request;
    }

    public byte[] data() {
        return data.clone();
    }

    public Instant readTime() {
        return readTime;
    }

    public int length() {
        return data.length;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SectorReadResult that)) {
            return false;
        }
        return request.equals(that.request)
                && Arrays.equals(data, that.data)
                && readTime.equals(that.readTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(request, Arrays.hashCode(data), readTime);
    }
}