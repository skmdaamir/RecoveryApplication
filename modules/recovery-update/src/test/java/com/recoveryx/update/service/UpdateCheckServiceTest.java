package com.recoveryx.update.service;

import com.recoveryx.update.model.UpdateInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdateCheckServiceTest {

    private final UpdateCheckService updateService = new UpdateCheckService();

    @Test
    void shouldCheckForUpdates() {
        UpdateInfo info = updateService.checkForUpdates();

        assertNotNull(info);
        assertNotNull(info.currentVersion());
        assertNotNull(info.latestVersion());
        assertNotNull(info.downloadUrl());
    }
}
