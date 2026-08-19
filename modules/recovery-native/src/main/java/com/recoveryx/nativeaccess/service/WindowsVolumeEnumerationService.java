package com.recoveryx.nativeaccess.service;

import com.recoveryx.common.exception.DeviceAccessException;
import com.recoveryx.nativeaccess.jna.Kernel32Ex;
import com.recoveryx.nativeaccess.model.NativeVolumeInfo;
import com.recoveryx.nativeaccess.util.MultiStringDecoder;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Enumerates Windows volumes and mounted paths using volume GUID APIs.
 */
@Service
public class WindowsVolumeEnumerationService {

    private static final int INITIAL_BUFFER_SIZE = 1024;
    private static final int MAX_VOLUME_NAME = 260;
    private static final int ERROR_NO_MORE_FILES = 18;
    private static final int ERROR_MORE_DATA = 234;

    private final WindowsPrivilegeService privilegeService;

    public WindowsVolumeEnumerationService(WindowsPrivilegeService privilegeService) {
        this.privilegeService = Objects.requireNonNull(privilegeService, "privilegeService must not be null");
    }

    public List<NativeVolumeInfo> enumerateVolumes() {
        privilegeService.requireWindows();

        List<NativeVolumeInfo> volumes = new ArrayList<>();
        char[] volumeBuffer = new char[MAX_VOLUME_NAME];
        WinNT.HANDLE findHandle = Kernel32Ex.INSTANCE.FindFirstVolumeW(volumeBuffer, volumeBuffer.length);

        if (findHandle == null || WinBase.INVALID_HANDLE_VALUE.equals(findHandle)) {
            throw new DeviceAccessException(
                    "Failed to enumerate volumes. Windows error code: " + Kernel32Ex.INSTANCE.GetLastError());
        }

        try {
            while (true) {
                String volumeGuidPath = Native.toString(volumeBuffer);
                if (!volumeGuidPath.isBlank()) {
                    volumes.add(new NativeVolumeInfo(
                            volumeGuidPath,
                            resolveDeviceName(volumeGuidPath),
                            resolveMountPaths(volumeGuidPath)));
                }

                char[] nextBuffer = new char[MAX_VOLUME_NAME];
                boolean hasNext = Kernel32Ex.INSTANCE.FindNextVolumeW(findHandle, nextBuffer, nextBuffer.length);
                if (!hasNext) {
                    int errorCode = Kernel32Ex.INSTANCE.GetLastError();
                    if (errorCode == ERROR_NO_MORE_FILES) {
                        break;
                    }
                    throw new DeviceAccessException(
                            "Failed while iterating volumes. Windows error code: " + errorCode);
                }
                volumeBuffer = nextBuffer;
            }
        } finally {
            Kernel32Ex.INSTANCE.FindVolumeClose(findHandle);
        }

        return List.copyOf(volumes);
    }

    private String resolveDeviceName(String volumeGuidPath) {
        String trimmed = trimVolumeGuidForQueryDosDevice(volumeGuidPath);
        char[] targetPath = new char[INITIAL_BUFFER_SIZE];
        int length = Kernel32Ex.INSTANCE.QueryDosDeviceW(trimmed, targetPath, targetPath.length);
        if (length == 0) {
            throw new DeviceAccessException(
                    "Failed to resolve device name for volume " + volumeGuidPath
                            + ". Windows error code: " + Kernel32Ex.INSTANCE.GetLastError());
        }
        return Native.toString(targetPath);
    }

    private List<String> resolveMountPaths(String volumeGuidPath) {
        int bufferSize = INITIAL_BUFFER_SIZE;

        while (true) {
            char[] namesBuffer = new char[bufferSize];
            IntByReference returnLength = new IntByReference();

            boolean success = Kernel32Ex.INSTANCE.GetVolumePathNamesForVolumeNameW(
                    Native.toCharArray(volumeGuidPath),
                    namesBuffer,
                    namesBuffer.length,
                    returnLength
            );

            if (success) {
                return List.copyOf(MultiStringDecoder.decode(namesBuffer, returnLength.getValue()));
            }

            int errorCode = Kernel32Ex.INSTANCE.GetLastError();
            if (errorCode != ERROR_MORE_DATA) {
                throw new DeviceAccessException(
                        "Failed to resolve mount paths for volume " + volumeGuidPath
                                + ". Windows error code: " + errorCode);
            }

            bufferSize = Math.max(bufferSize * 2, returnLength.getValue());
        }
    }

    private String trimVolumeGuidForQueryDosDevice(String volumeGuidPath) {
        if (volumeGuidPath == null || volumeGuidPath.length() < 5) {
            throw new IllegalArgumentException("volumeGuidPath is invalid");
        }

        String value = volumeGuidPath;
        if (value.endsWith("\\")) {
            value = value.substring(0, value.length() - 1);
        }
        if (!value.startsWith("\\\\?\\")) {
            throw new IllegalArgumentException("volumeGuidPath must start with \\\\?\\");
        }
        return value.substring(4);
    }
}