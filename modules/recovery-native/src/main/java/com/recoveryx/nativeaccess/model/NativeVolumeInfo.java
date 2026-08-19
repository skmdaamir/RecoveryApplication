package com.recoveryx.nativeaccess.model;

import java.util.List;

/**
 * Native Windows volume information resolved from the volume enumeration APIs.
 *
 * @param volumeGuidPath volume GUID path like \\?\Volume{...}\
 * @param deviceName     underlying device name
 * @param mountPaths     mounted paths such as C:\
 */
public record NativeVolumeInfo(String volumeGuidPath,String deviceName,List<String>mountPaths){

public NativeVolumeInfo{mountPaths=mountPaths==null?List.of():List.copyOf(mountPaths);}}