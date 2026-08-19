package com.recoveryx.core.port.recovery;

import com.recoveryx.core.domain.recovery.RecoveryRequest;
import com.recoveryx.core.domain.recovery.RecoveryResult;

/**
 * Coordinates the recovery lifecycle.
 */
public interface RecoveryOrchestratorPort {

    /**
     * Executes recovery for selected files.
     *
     * @param request recovery request
     * @return recovery result summary
     */
    RecoveryResult recover(RecoveryRequest request);
}