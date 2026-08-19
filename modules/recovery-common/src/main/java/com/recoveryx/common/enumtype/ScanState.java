package com.recoveryx.common.enumtype;

/**
 * Lifecycle state of an active or persisted scan.
 */
public enum ScanState {
    CREATED,
    QUEUED,
    RUNNING,
    PAUSED,
    STOPPED,
    COMPLETED,
    FAILED,
    CANCELLED
}