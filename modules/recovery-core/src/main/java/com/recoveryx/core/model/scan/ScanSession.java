package com.recoveryx.core.model.scan;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a created scan session.
 */
public final class ScanSession {

    private final String sessionId;
    private final ScanRequest scanRequest;
    private final Instant createdAt;
    private final String status;

    public ScanSession(String sessionId, ScanRequest scanRequest, Instant createdAt, String status) {
        this.sessionId = sessionId == null || sessionId.isBlank() ? UUID.randomUUID().toString() : sessionId;
        this.scanRequest = Objects.requireNonNull(scanRequest, "scanRequest");
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.status = status == null || status.isBlank() ? "CREATED" : status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public ScanRequest getScanRequest() {
        return scanRequest;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }
}