package com.recoveryx.core.domain.scan;

import com.recoveryx.common.enumtype.SessionState;
import com.recoveryx.common.util.ValidationUtils;

import java.time.Instant;

/**
 * Persistable scan session metadata.
 *
 * @param sessionId session identifier
 * @param scanRequest original scan request
 * @param state persisted session state
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
public record ScanSession(
        String sessionId,
        ScanRequest scanRequest,
        SessionState state,
        Instant createdAt,
        Instant updatedAt) {

    public ScanSession {
        ValidationUtils.requireNotBlank(sessionId, "sessionId");
        ValidationUtils.requireNonNull(scanRequest, "scanRequest");
        ValidationUtils.requireNonNull(state, "state");
        ValidationUtils.requireNonNull(createdAt, "createdAt");
        ValidationUtils.requireNonNull(updatedAt, "updatedAt");
    }
}