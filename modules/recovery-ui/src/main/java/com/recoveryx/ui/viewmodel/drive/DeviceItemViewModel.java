package com.recoveryx.ui.viewmodel.drive;

import com.recoveryx.core.model.device.StorageDevice;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.text.DecimalFormat;
import java.util.Objects;

/**
 * Presentation model for a single device row in the Drive Selection table.
 */
public final class DeviceItemViewModel {

    private final StorageDevice device;

    private final StringProperty displayName = new SimpleStringProperty();
    private final StringProperty devicePath = new SimpleStringProperty();
    private final StringProperty deviceType = new SimpleStringProperty();
    private final StringProperty fileSystem = new SimpleStringProperty();
    private final StringProperty sizeText = new SimpleStringProperty();
    private final StringProperty accessibilityText = new SimpleStringProperty();

    public DeviceItemViewModel(StorageDevice device) {
        this.device = Objects.requireNonNull(device, "device");
        refreshProperties();
    }

    public StorageDevice getDevice() {
        return device;
    }

    public StringProperty displayNameProperty() {
        return displayName;
    }

    public StringProperty devicePathProperty() {
        return devicePath;
    }

    public StringProperty deviceTypeProperty() {
        return deviceType;
    }

    public StringProperty fileSystemProperty() {
        return fileSystem;
    }

    public StringProperty sizeTextProperty() {
        return sizeText;
    }

    public StringProperty accessibilityTextProperty() {
        return accessibilityText;
    }

    public String getDisplayName() {
        return displayName.get();
    }

    public String getDevicePath() {
        return devicePath.get();
    }

    public String getDeviceType() {
        return deviceType.get();
    }

    public String getFileSystem() {
        return fileSystem.get();
    }

    public String getSizeText() {
        return sizeText.get();
    }

    public String getAccessibilityText() {
        return accessibilityText.get();
    }

    private void refreshProperties() {
        displayName.set(safeString(device.getDisplayName()));
        devicePath.set(safeString(device.getDevicePath()));
        deviceType.set(safeString(device.getDeviceType().name()));
        fileSystem.set(safeString(device.getFileSystemType().name()));
        sizeText.set(formatBytes(device.getTotalBytes()));
        accessibilityText.set(device.isReadable() ? "Readable" : "Unavailable");
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private String formatBytes(long bytes) {
        if (bytes <= 0L) {
            return "0 B";
        }
        String[] units = {"B", "KB", "MB", "GB", "TB", "PB"};
        double value = bytes;
        int unitIndex = 0;
        while (value >= 1024.0 && unitIndex < units.length - 1) {
            value /= 1024.0;
            unitIndex++;
        }
        return new DecimalFormat("#,##0.##").format(value) + " " + units[unitIndex];
    }
}