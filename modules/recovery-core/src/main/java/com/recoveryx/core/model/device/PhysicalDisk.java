package com.recoveryx.core.model.device;

import java.util.Objects;


public record PhysicalDisk(int diskNumber,String devicePath,String displayName,String vendor,String product,String serialNumber,long sizeBytes,int bytesPerSector,boolean removable,DeviceBusType busType,DeviceCategory category){

public PhysicalDisk{if(diskNumber<0){throw new IllegalArgumentException("diskNumber must be >= 0");}Objects.requireNonNull(devicePath,"devicePath must not be null");Objects.requireNonNull(displayName,"displayName must not be null");Objects.requireNonNull(vendor,"vendor must not be null");Objects.requireNonNull(product,"product must not be null");Objects.requireNonNull(serialNumber,"serialNumber must not be null");Objects.requireNonNull(busType,"busType must not be null");Objects.requireNonNull(category,"category must not be null");if(sizeBytes<0){throw new IllegalArgumentException("sizeBytes must be >= 0");}if(bytesPerSector<=0){throw new IllegalArgumentException("bytesPerSector must be > 0");}}}