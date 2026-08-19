package com.recoveryx.core.port.scan;

import com.recoveryx.core.domain.scan.ScanSession;

import java.util.Optional;

/**
 * Persists and restores scan sessions.
 */
public interface ScanSessionPort {

    /**
     * Saves a scan session.
     *
     * @param scanSession session state
     */
    void save(ScanSession scanSession);

    /**
     * Finds session by identifier.
     *
     * @param sessionId session identifier
     * @return optional session
     */
    Optional<ScanSession> findById(String sessionId);
}