package com.recoveryx.core.usecase;

import com.recoveryx.core.domain.scan.ScanSession;

import java.util.Optional;

/**
 * Use case for resuming a persisted scan session.
 */
public interface ResumeSessionUseCase {

    /**
     * Resumes a scan session.
     *
     * @param sessionId session identifier
     * @return resolved session if present
     */
    Optional<ScanSession> execute(String sessionId);
}