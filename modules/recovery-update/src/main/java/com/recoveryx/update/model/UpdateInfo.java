package com.recoveryx.update.model;

import com.recoveryx.common.util.ValidationUtils;

/**
 * Metadata response for update availability checks.
 */
public record UpdateInfo(
        String currentVersion,
        String latestVersion,
        boolean updateAvailable,
        String releaseNotesUrl,
        String downloadUrl) {

    public UpdateInfo {
        ValidationUtils.requireNotBlank(currentVersion, "currentVersion");
        ValidationUtils.requireNotBlank(latestVersion, "latestVersion");
    }
}
