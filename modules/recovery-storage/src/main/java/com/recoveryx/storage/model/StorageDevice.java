package com.recoveryx.storage.model;

import java.util.Objects;

/**
 * Physical storage device metadata exposed by the storage layer.
 */
public final class StorageDevice {

    private final String devicePath;
    private final String displayName;
    private final String vendor;
    private final String product;
    private final String serialNumber;
    private final StorageDeviceType deviceType;
    private final boolean removable;
    private final DeviceGeometry geometry;
    private final int deviceNumber;
    private final int partitionNumber;

    public StorageDevice(String devicePath,
            String displayName,
            String vendor,
            String product,
            String serialNumber,
            StorageDeviceType deviceType,
            boolean removable,
            DeviceGeometry geometry,
            int deviceNumber,
            int partitionNumber) {
        this.devicePath = Objects.requireNonNull(devicePath, "devicePath must not be null");
        this.displayName = Objects.requireNonNull(displayName, "displayName must not be null");
        this.vendor = vendor == null ? "" : vendor;
        this.product = product == null ? "" : product;
        this.serialNumber = serialNumber == null ? "" : serialNumber;
        this.deviceType = Objects.requireNonNull(deviceType, "deviceType must not be null");
        this.geometry = Objects.requireNonNull(geometry, "geometry must not be null");
        this.removable = removable;
        this.deviceNumber = deviceNumber;
        this.partitionNumber = partitionNumber;
    }

    public String devicePath() {
        return devicePath;
    }

    public String displayName() {
        return displayName;
    }

    public String vendor() {
        return vendor;
    }

    public String product() {
        return product;
    }

    public String serialNumber() {
        return serialNumber;
    }

    public StorageDeviceType deviceType() {
        return deviceType;
    }

    public boolean removable() {
        return removable;
    }

    public DeviceGeometry geometry() {
        return geometry;
    }

    public int deviceNumber() {
        return deviceNumber;
    }

    public int partitionNumber() {
        return partitionNumber;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof StorageDevice that)) {
            return false;
        }
        return removable == that.removable
                && deviceNumber == that.deviceNumber
                && partitionNumber == that.partitionNumber
                && devicePath.equals(that.devicePath)
                && displayName.equals(that.displayName)
                && vendor.equals(that.vendor)
                && product.equals(that.product)
                && serialNumber.equals(that.serialNumber)
                && deviceType == that.deviceType
                && geometry.equals(that.geometry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(devicePath, displayName, vendor, product, serialNumber,
                deviceType, removable, geometry, deviceNumber, partitionNumber);
    }
}