package com.recoveryx.ui.viewmodel.drive;

import com.recoveryx.core.model.device.DeviceHealth;
import com.recoveryx.core.model.device.DeviceType;
import com.recoveryx.core.model.device.FileSystemType;
import com.recoveryx.core.model.device.StorageDevice;
import com.recoveryx.core.model.scan.ScanMode;
import com.recoveryx.core.model.scan.ScanRequest;
import com.recoveryx.core.model.scan.ScanSession;
import com.recoveryx.core.service.ScanOrchestrationService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

/**
 * ViewModel for selecting a raw disk image file (.img, .dd, .iso, .raw, .bin)
 * directly from the filesystem and initiating a deep scan without physical media.
 */
public class DiskImageSelectionViewModel {

    private final ScanOrchestrationService scanOrchestrationService;

    private final ObjectProperty<File> selectedImageFile = new SimpleObjectProperty<>();
    private final StringProperty imagePathText = new SimpleStringProperty("");
    private final StringProperty imageInfoText = new SimpleStringProperty("No image file loaded");
    private final BooleanProperty readyToScan = new SimpleBooleanProperty(false);
    private final StringProperty statusMessage = new SimpleStringProperty("Select a disk image file (.img, .dd, .iso, .raw)");

    public DiskImageSelectionViewModel(ScanOrchestrationService scanOrchestrationService) {
        this.scanOrchestrationService = Objects.requireNonNull(scanOrchestrationService, "scanOrchestrationService");
    }

    public ObjectProperty<File> selectedImageFileProperty() {
        return selectedImageFile;
    }

    public StringProperty imagePathTextProperty() {
        return imagePathText;
    }

    public StringProperty imageInfoTextProperty() {
        return imageInfoText;
    }

    public BooleanProperty readyToScanProperty() {
        return readyToScan;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    /**
     * Loads and validates the selected image file.
     */
    public void selectFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            selectedImageFile.set(null);
            imagePathText.set("");
            imageInfoText.set("Invalid or non-existent file");
            readyToScan.set(false);
            statusMessage.set("Please select a valid disk image file");
            return;
        }

        selectedImageFile.set(file);
        imagePathText.set(file.getAbsolutePath());
        long sizeBytes = file.length();
        imageInfoText.set(String.format("File: %s | Size: %s", file.getName(), formatSize(sizeBytes)));
        readyToScan.set(true);
        statusMessage.set("Ready to scan image: " + file.getName());
    }

    /**
     * Creates a deep scan session targeting the loaded disk image file.
     */
    public ScanSession startImageScan() {
        File file = selectedImageFile.get();
        if (file == null || !file.exists()) {
            throw new IllegalStateException("No valid image file selected");
        }

        StorageDevice virtualDevice = StorageDevice.builder()
                .deviceId(UUID.randomUUID().toString())
                .displayName("Disk Image: " + file.getName())
                .devicePath(file.getAbsolutePath())
                .deviceType(DeviceType.UNKNOWN)
                .fileSystemType(FileSystemType.UNKNOWN)
                .health(DeviceHealth.HEALTHY)
                .totalBytes(file.length())
                .freeBytes(0L)
                .readable(true)
                .removable(true)
                .systemDevice(false)
                .build();

        ScanRequest request = ScanRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .storageDevice(virtualDevice)
                .scanMode(ScanMode.DEEP)
                .includeDeletedEntries(true)
                .includeRawSignatureScan(true)
                .build();

        return scanOrchestrationService.createSession(request);
    }

    private static String formatSize(long bytes) {
        if (bytes >= 1_073_741_824L) {
            return String.format("%.2f GB", bytes / 1_073_741_824.0);
        } else if (bytes >= 1_048_576L) {
            return String.format("%.2f MB", bytes / 1_048_576.0);
        } else if (bytes >= 1024L) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return bytes + " B";
        }
    }
}
