package com.recoveryx.ui.config;

import com.recoveryx.common.constant.RecoveryConstants;
import com.recoveryx.common.enumtype.ApplicationTheme;
import com.recoveryx.common.enumtype.LanguageCode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Validated application properties for the RecoveryX Pro desktop application.
 */
@Validated
@ConfigurationProperties(prefix = "recoveryx.app")
public class RecoveryAppProperties {

    private LanguageCode language = LanguageCode.EN;
    private ApplicationTheme theme = ApplicationTheme.SYSTEM;

    @Min(RecoveryConstants.MIN_SCAN_THREADS)
    @Max(RecoveryConstants.MAX_SCAN_THREADS)
    private int scanThreads = RecoveryConstants.DEFAULT_SCAN_THREADS;

    @Min(RecoveryConstants.MIN_READ_BUFFER_SIZE)
    @Max(RecoveryConstants.MAX_READ_BUFFER_SIZE)
    private int bufferSizeBytes = RecoveryConstants.DEFAULT_READ_BUFFER_SIZE;

    @NotBlank
    private String recoveryDirectory = RecoveryConstants.DEFAULT_RECOVERY_DIRECTORY;

    @NotBlank
    private String sessionDirectory = RecoveryConstants.DEFAULT_SESSION_DIRECTORY;

    @NotBlank
    private String reportDirectory = RecoveryConstants.DEFAULT_REPORT_DIRECTORY;

    private boolean autoSaveEnabled = true;
    private boolean autoUpdateEnabled = true;

    public LanguageCode getLanguage() {
        return language;
    }

    public void setLanguage(LanguageCode language) {
        this.language = language;
    }

    public ApplicationTheme getTheme() {
        return theme;
    }

    public void setTheme(ApplicationTheme theme) {
        this.theme = theme;
    }

    public int getScanThreads() {
        return scanThreads;
    }

    public void setScanThreads(int scanThreads) {
        this.scanThreads = scanThreads;
    }

    public int getBufferSizeBytes() {
        return bufferSizeBytes;
    }

    public void setBufferSizeBytes(int bufferSizeBytes) {
        this.bufferSizeBytes = bufferSizeBytes;
    }

    public String getRecoveryDirectory() {
        return recoveryDirectory;
    }

    public void setRecoveryDirectory(String recoveryDirectory) {
        this.recoveryDirectory = recoveryDirectory;
    }

    public String getSessionDirectory() {
        return sessionDirectory;
    }

    public void setSessionDirectory(String sessionDirectory) {
        this.sessionDirectory = sessionDirectory;
    }

    public String getReportDirectory() {
        return reportDirectory;
    }

    public void setReportDirectory(String reportDirectory) {
        this.reportDirectory = reportDirectory;
    }

    public boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    public void setAutoSaveEnabled(boolean autoSaveEnabled) {
        this.autoSaveEnabled = autoSaveEnabled;
    }

    public boolean isAutoUpdateEnabled() {
        return autoUpdateEnabled;
    }

    public void setAutoUpdateEnabled(boolean autoUpdateEnabled) {
        this.autoUpdateEnabled = autoUpdateEnabled;
    }
}