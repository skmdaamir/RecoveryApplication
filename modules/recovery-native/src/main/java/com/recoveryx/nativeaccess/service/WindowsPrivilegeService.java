package com.recoveryx.nativeaccess.service;

import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Provides Windows privilege and platform checks for raw disk access.
 */
@Service
public class WindowsPrivilegeService {

    public boolean isWindows() {
        return System.getProperty("os.name", "")
                .toLowerCase(Locale.ROOT)
                .contains("win");
    }

    public void requireWindows() {
        if (!isWindows()) {
            throw new IllegalStateException("RecoveryX Pro native disk access is only supported on Windows");
        }
    }
}