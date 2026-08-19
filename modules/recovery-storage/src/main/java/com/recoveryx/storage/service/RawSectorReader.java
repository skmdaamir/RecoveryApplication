package com.recoveryx.storage.service;

import com.recoveryx.storage.model.SectorReadRequest;
import com.recoveryx.storage.model.SectorReadResult;

/**
 * Reads aligned raw sectors from a device.
 */
public interface RawSectorReader {

    SectorReadResult read(SectorReadRequest request);
}