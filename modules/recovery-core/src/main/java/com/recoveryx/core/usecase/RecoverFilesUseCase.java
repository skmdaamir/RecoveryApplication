package com.recoveryx.core.usecase;

import com.recoveryx.core.domain.recovery.RecoveryRequest;
import com.recoveryx.core.domain.recovery.RecoveryResult;

/**
 * Use case for recovering selected files.
 */
public interface RecoverFilesUseCase {

    /**
     * Recovers files based on a request.
     *
     * @param request recovery request
     * @return recovery result
     */
    RecoveryResult execute(RecoveryRequest request);
}