package com.recoveryx.core.service;

import com.recoveryx.core.model.scan.ScanRequest;
import com.recoveryx.core.model.scan.ScanSession;



public interface ScanOrchestrationService {

    ScanSession createSession(ScanRequest request);
}