package com.recoveryx.storage.service;

import com.recoveryx.nativeaccess.model.NativeDeviceHandle;
import com.recoveryx.nativeaccess.service.WindowsNativeDiskAccessService;
import com.recoveryx.storage.model.DeviceReadSession;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Creates and closes device read sessions for later scanner and parser use.
 */
@Service
public class DeviceReadSessionFactory {

    private final WindowsNativeDiskAccessService nativeDiskAccessService;

    public DeviceReadSessionFactory(WindowsNativeDiskAccessService nativeDiskAccessService) {
        this.nativeDiskAccessService = Objects.requireNonNull(nativeDiskAccessService,
                "nativeDiskAccessService must not be null");
    }

    public DeviceReadSession open(String devicePath) {
        NativeDeviceHandle handle = nativeDiskAccessService.openReadOnly(devicePath);
        return new DeviceReadSession(devicePath, handle.bytesPerSector(), handle);
    }

    public void close(DeviceReadSession session) {
        if (session == null) {
            return;
        }
        nativeDiskAccessService.close(session.handle());
    }
}