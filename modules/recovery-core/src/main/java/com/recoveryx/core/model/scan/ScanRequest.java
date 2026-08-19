package com.recoveryx.core.model.scan;

import com.recoveryx.core.model.device.StorageDevice;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable request to create a scan session.
 */
public final class ScanRequest {

    private final String requestId;
    private final StorageDevice storageDevice;
    private final ScanMode scanMode;
    private final Instant requestedAt;
    private final boolean includeDeletedEntries;
    private final boolean includeRawSignatureScan;

    private ScanRequest(Builder builder) {
        this.requestId = builder.requestId == null || builder.requestId.isBlank()
                ? UUID.randomUUID().toString()
                : builder.requestId;
        this.storageDevice = Objects.requireNonNull(builder.storageDevice, "storageDevice");
        this.scanMode = Objects.requireNonNull(builder.scanMode, "scanMode");
        this.requestedAt = builder.requestedAt == null ? Instant.now() : builder.requestedAt;
        this.includeDeletedEntries = builder.includeDeletedEntries;
        this.includeRawSignatureScan = builder.includeRawSignatureScan;
    }

    public String getRequestId() {
        return requestId;
    }

    public StorageDevice getStorageDevice() {
        return storageDevice;
    }

    public ScanMode getScanMode() {
        return scanMode;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public boolean isIncludeDeletedEntries() {
        return includeDeletedEntries;
    }

    public boolean isIncludeRawSignatureScan() {
        return includeRawSignatureScan;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String requestId;
        private StorageDevice storageDevice;
        private ScanMode scanMode = ScanMode.QUICK;
        private Instant requestedAt;
        private boolean includeDeletedEntries = true;
        private boolean includeRawSignatureScan;

        private Builder() {
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder storageDevice(StorageDevice storageDevice) {
            this.storageDevice = storageDevice;
            return this;
        }

        public Builder scanMode(ScanMode scanMode) {
            this.scanMode = scanMode;
            return this;
        }

        public Builder requestedAt(Instant requestedAt) {
            this.requestedAt = requestedAt;
            return this;
        }

        public Builder includeDeletedEntries(boolean includeDeletedEntries) {
            this.includeDeletedEntries = includeDeletedEntries;
            return this;
        }

        public Builder includeRawSignatureScan(boolean includeRawSignatureScan) {
            this.includeRawSignatureScan = includeRawSignatureScan;
            return this;
        }

        public ScanRequest build() {
            return new ScanRequest(this);
        }
    }
}