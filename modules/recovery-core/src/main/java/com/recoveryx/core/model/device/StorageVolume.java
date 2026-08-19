package com.recoveryx.core.model.device;

import java.util.List;
import java.util.Objects;

/**
 * Logical volume inventory model.
 *
 * @param volumeGuidPath     volume GUID path
 * @param deviceName         native volume device name
 * @param mountPaths         assigned mount paths
 * @param physicalDiskNumber physical disk number if resolved
 * @param partitionNumber    partition number if resolved
 * @param status             volume status
 */
public record StorageVolume(String volumeGuidPath,String deviceName,List<String>mountPaths,Integer physicalDiskNumber,Integer partitionNumber,VolumeStatus status){

public StorageVolume{Objects.requireNonNull(volumeGuidPath,"volumeGuidPath must not be null");Objects.requireNonNull(deviceName,"deviceName must not be null");mountPaths=mountPaths==null?List.of():List.copyOf(mountPaths);Objects.requireNonNull(status,"status must not be null");}}