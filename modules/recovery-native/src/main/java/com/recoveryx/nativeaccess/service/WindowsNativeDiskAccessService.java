package com.recoveryx.nativeaccess.service;

import com.recoveryx.common.exception.DeviceAccessException;
import com.recoveryx.nativeaccess.constant.WinIoctlConstants;
import com.recoveryx.nativeaccess.jna.Kernel32Ex;
import com.recoveryx.nativeaccess.jna.WinIoctlStructures;
import com.recoveryx.nativeaccess.model.NativeDeviceHandle;
import com.recoveryx.nativeaccess.model.NativeDriveGeometry;
import com.recoveryx.nativeaccess.model.NativeStorageDeviceDescriptor;
import com.recoveryx.nativeaccess.model.NativeStorageDeviceNumber;
import com.recoveryx.nativeaccess.model.RawReadRequest;
import com.recoveryx.nativeaccess.util.AlignmentUtils;
import com.recoveryx.nativeaccess.util.NativeErrorUtils;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Read-only Windows raw disk access service backed by CreateFile, ReadFile, and
 * DeviceIoControl.
 */
@Service
public class WindowsNativeDiskAccessService {

    private static final int OPEN_EXISTING = 3;
    private static final int FILE_SHARE_READ = 0x00000001;
    private static final int FILE_SHARE_WRITE = 0x00000002;
    private static final int GENERIC_READ = 0x80000000;
    private static final int FILE_BEGIN = 0;
    private static final int FILE_ATTRIBUTE_NORMAL = 0x00000080;

    private final WindowsPrivilegeService privilegeService;

    public WindowsNativeDiskAccessService(WindowsPrivilegeService privilegeService) {
        this.privilegeService = Objects.requireNonNull(privilegeService, "privilegeService must not be null");
    }

    public NativeDeviceHandle openReadOnly(String devicePath) {
        privilegeService.requireWindows();

        char[] path = Native.toCharArray(devicePath);
        WinNT.HANDLE handle = Kernel32Ex.INSTANCE.CreateFileW(
                path,
                GENERIC_READ,
                FILE_SHARE_READ | FILE_SHARE_WRITE,
                Pointer.NULL,
                OPEN_EXISTING,
                FILE_ATTRIBUTE_NORMAL,
                null);

        if (handle == null || WinBase.INVALID_HANDLE_VALUE.equals(handle)) {
            throw NativeErrorUtils.deviceAccessFailure("openReadOnly", devicePath, Kernel32Ex.INSTANCE.GetLastError());
        }

        NativeDriveGeometry geometry = queryGeometry(handle, devicePath);
        return new NativeDeviceHandle(handle, devicePath, geometry.bytesPerSector());
    }

    public NativeDriveGeometry queryGeometry(NativeDeviceHandle deviceHandle) {
        return queryGeometry(deviceHandle.handle(), deviceHandle.devicePath());
    }

    public NativeStorageDeviceDescriptor queryStorageDescriptor(NativeDeviceHandle deviceHandle) {
        WinIoctlStructures.STORAGE_PROPERTY_QUERY query = new WinIoctlStructures.STORAGE_PROPERTY_QUERY();
        query.PropertyId = WinIoctlConstants.STORAGE_DEVICE_PROPERTY;
        query.QueryType = WinIoctlConstants.PROPERTY_STANDARD_QUERY;
        query.write();

        Memory headerBuffer = new Memory(8);
        IntByReference bytesReturned = new IntByReference();

        boolean headerResult = Kernel32Ex.INSTANCE.DeviceIoControl(
                deviceHandle.handle(),
                WinIoctlConstants.IOCTL_STORAGE_QUERY_PROPERTY,
                query.getPointer(),
                query.size(),
                headerBuffer,
                (int) headerBuffer.size(),
                bytesReturned,
                null);

        if (!headerResult) {
            throw NativeErrorUtils.deviceAccessFailure(
                    "queryStorageDescriptor.header",
                    deviceHandle.devicePath(),
                    Kernel32Ex.INSTANCE.GetLastError());
        }

        WinIoctlStructures.STORAGE_DESCRIPTOR_HEADER header = new WinIoctlStructures.STORAGE_DESCRIPTOR_HEADER(
                headerBuffer);
        header.read();

        Memory descriptorBuffer = new Memory(header.Size);
        boolean result = Kernel32Ex.INSTANCE.DeviceIoControl(
                deviceHandle.handle(),
                WinIoctlConstants.IOCTL_STORAGE_QUERY_PROPERTY,
                query.getPointer(),
                query.size(),
                descriptorBuffer,
                (int) descriptorBuffer.size(),
                bytesReturned,
                null);

        if (!result) {
            throw NativeErrorUtils.deviceAccessFailure(
                    "queryStorageDescriptor.body",
                    deviceHandle.devicePath(),
                    Kernel32Ex.INSTANCE.GetLastError());
        }

        WinIoctlStructures.STORAGE_DEVICE_DESCRIPTOR descriptor = new WinIoctlStructures.STORAGE_DEVICE_DESCRIPTOR(
                descriptorBuffer);
        descriptor.read();

        return new NativeStorageDeviceDescriptor(
                WinIoctlStructures.STORAGE_DEVICE_DESCRIPTOR.readNullTerminatedAscii(descriptorBuffer,
                        descriptor.VendorIdOffset),
                WinIoctlStructures.STORAGE_DEVICE_DESCRIPTOR.readNullTerminatedAscii(descriptorBuffer,
                        descriptor.ProductIdOffset),
                WinIoctlStructures.STORAGE_DEVICE_DESCRIPTOR.readNullTerminatedAscii(descriptorBuffer,
                        descriptor.ProductRevisionOffset),
                WinIoctlStructures.STORAGE_DEVICE_DESCRIPTOR.readNullTerminatedAscii(descriptorBuffer,
                        descriptor.SerialNumberOffset),
                descriptor.RemovableMedia,
                descriptor.BusType);
    }

    public NativeStorageDeviceNumber queryDeviceNumber(String devicePath) {
        NativeDeviceHandle handle = openReadOnly(devicePath);
        try {
            Memory outputBuffer = new Memory(new WinIoctlStructures.STORAGE_DEVICE_NUMBER().size());
            IntByReference bytesReturned = new IntByReference();

            boolean result = Kernel32Ex.INSTANCE.DeviceIoControl(
                    handle.handle(),
                    WinIoctlConstants.IOCTL_STORAGE_GET_DEVICE_NUMBER,
                    Pointer.NULL,
                    0,
                    outputBuffer,
                    (int) outputBuffer.size(),
                    bytesReturned,
                    null);

            if (!result) {
                throw NativeErrorUtils.deviceAccessFailure(
                        "queryDeviceNumber",
                        devicePath,
                        Kernel32Ex.INSTANCE.GetLastError());
            }

            WinIoctlStructures.STORAGE_DEVICE_NUMBER deviceNumber = new WinIoctlStructures.STORAGE_DEVICE_NUMBER(
                    outputBuffer);
            deviceNumber.read();

            return new NativeStorageDeviceNumber(
                    deviceNumber.DeviceType,
                    deviceNumber.DeviceNumber,
                    deviceNumber.PartitionNumber);
        } finally {
            close(handle);
        }
    }

    public byte[] read(NativeDeviceHandle deviceHandle, RawReadRequest request) {
        Objects.requireNonNull(deviceHandle, "deviceHandle must not be null");
        Objects.requireNonNull(request, "request must not be null");

        AlignmentUtils.requireAligned(request.offsetBytes(), request.lengthBytes(), deviceHandle.bytesPerSector());

        seek(deviceHandle, request.offsetBytes());

        Memory memory = new Memory(request.lengthBytes());
        IntByReference bytesRead = new IntByReference();

        boolean result = Kernel32Ex.INSTANCE.ReadFile(
                deviceHandle.handle(),
                memory,
                request.lengthBytes(),
                bytesRead,
                null);

        if (!result) {
            throw NativeErrorUtils.deviceAccessFailure(
                    "read",
                    deviceHandle.devicePath(),
                    Kernel32Ex.INSTANCE.GetLastError());
        }

        if (bytesRead.getValue() != request.lengthBytes()) {
            throw new DeviceAccessException(
                    "Unexpected byte count while reading %s. Expected %d but got %d"
                            .formatted(deviceHandle.devicePath(), request.lengthBytes(), bytesRead.getValue()));
        }

        return memory.getByteArray(0, request.lengthBytes());
    }

    public void close(NativeDeviceHandle handle) {
        if (handle == null || handle.handle() == null) {
            return;
        }
        Kernel32Ex.INSTANCE.CloseHandle(handle.handle());
    }

    private void seek(NativeDeviceHandle deviceHandle, long offsetBytes) {
        Kernel32Ex.LongByReferenceEx newPosition = new Kernel32Ex.LongByReferenceEx();
        boolean result = Kernel32Ex.INSTANCE.SetFilePointerEx(
                deviceHandle.handle(),
                offsetBytes,
                newPosition,
                FILE_BEGIN);

        if (!result) {
            throw NativeErrorUtils.deviceAccessFailure(
                    "seek",
                    deviceHandle.devicePath(),
                    Kernel32Ex.INSTANCE.GetLastError());
        }
    }

    private NativeDriveGeometry queryGeometry(WinNT.HANDLE handle, String devicePath) {
        Memory outputBuffer = new Memory(256);
        IntByReference bytesReturned = new IntByReference();

        boolean result = Kernel32Ex.INSTANCE.DeviceIoControl(
                handle,
                WinIoctlConstants.IOCTL_DISK_GET_DRIVE_GEOMETRY_EX,
                Pointer.NULL,
                0,
                outputBuffer,
                (int) outputBuffer.size(),
                bytesReturned,
                null);

        if (!result) {
            throw NativeErrorUtils.deviceAccessFailure(
                    "queryGeometry",
                    devicePath,
                    Kernel32Ex.INSTANCE.GetLastError());
        }

        WinIoctlStructures.DISK_GEOMETRY_EX geometryEx = new WinIoctlStructures.DISK_GEOMETRY_EX(outputBuffer);
        geometryEx.read();

        return new NativeDriveGeometry(
                geometryEx.Geometry.BytesPerSector.intValue(),
                geometryEx.Geometry.SectorsPerTrack.longValue(),
                geometryEx.Geometry.TracksPerCylinder.longValue(),
                geometryEx.DiskSize.getValue());
    }
}