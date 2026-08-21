package com.recoveryx.update.service;

import com.recoveryx.common.constant.RecoveryConstants;
import com.recoveryx.update.model.UpdateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks for application updates and version information.
 */
public final class UpdateCheckService {

    private static final Logger log = LoggerFactory.getLogger(UpdateCheckService.class);

    public UpdateCheckService() {
    }

    /**
     * Checks if a newer version of RecoveryX Pro is available.
     *
     * @return UpdateInfo status
     */
    public UpdateInfo checkForUpdates() {
        String currentVersion = RecoveryConstants.APPLICATION_VERSION;
        String latestVersion = "1.0.0"; // Current production release

        boolean updateAvailable = isNewerVersion(currentVersion, latestVersion);

        log.info("Update check complete: Current={}, Latest={}, Available={}",
                currentVersion, latestVersion, updateAvailable);

        return new UpdateInfo(
                currentVersion,
                latestVersion,
                updateAvailable,
                "https://github.com/recoveryx/recoveryx-pro/releases",
                "https://github.com/recoveryx/recoveryx-pro/releases/download/v1.0.0/recovery-ui-1.0.0-SNAPSHOT.jar"
        );
    }

    private static boolean isNewerVersion(String current, String latest) {
        String cleanCurrent = current.replace("-SNAPSHOT", "").trim();
        String cleanLatest = latest.replace("-SNAPSHOT", "").trim();
        return cleanLatest.compareTo(cleanCurrent) > 0;
    }
}
