package com.recoveryx.nativeaccess.jna;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT.LARGE_INTEGER;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Windows storage-related structures used by DeviceIoControl.
 */
public final class WinIoctlStructures {

    private WinIoctlStructures() {
    }

    public static class DISK_GEOMETRY extends Structure {
        public LARGE_INTEGER Cylinders;
        public DWORD MediaType;
        public DWORD TracksPerCylinder;
        public DWORD SectorsPerTrack;
        public DWORD BytesPerSector;

        public DISK_GEOMETRY() {
        }

        public DISK_GEOMETRY(Pointer pointer) {
            super(pointer);
        }

        @Override
        protected List<String> getFieldOrder() {
            return List.of("Cylinders", "MediaType", "TracksPerCylinder", "SectorsPerTrack", "BytesPerSector");
        }
    }

    public static class DISK_GEOMETRY_EX extends Structure {
        public DISK_GEOMETRY Geometry;
        public LARGE_INTEGER DiskSize;
        public byte[] Data = new byte[1];

        public DISK_GEOMETRY_EX() {
        }

        public DISK_GEOMETRY_EX(Pointer pointer) {
            super(pointer);
        }

        @Override
        protected List<String> getFieldOrder() {
            return List.of("Geometry", "DiskSize", "Data");
        }
    }

    public static class STORAGE_PROPERTY_QUERY extends Structure {
        public int PropertyId;
        public int QueryType;
        public byte[] AdditionalParameters = new byte[1];

        public STORAGE_PROPERTY_QUERY() {
        }

        public STORAGE_PROPERTY_QUERY(Pointer pointer) {
            super(pointer);
        }

        @Override
        protected List<String> getFieldOrder() {
            return List.of("PropertyId", "QueryType", "AdditionalParameters");
        }
    }

    public static class STORAGE_DESCRIPTOR_HEADER extends Structure {
        public int Version;
        public int Size;

        public STORAGE_DESCRIPTOR_HEADER() {
        }

        public STORAGE_DESCRIPTOR_HEADER(Pointer pointer) {
            super(pointer);
        }

        @Override
        protected List<String> getFieldOrder() {
            return List.of("Version", "Size");
        }
    }

    public static class STORAGE_DEVICE_DESCRIPTOR extends Structure {
        public int Version;
        public int Size;
        public byte DeviceType;
        public byte DeviceTypeModifier;
        public boolean RemovableMedia;
        public boolean CommandQueueing;
        public int VendorIdOffset;
        public int ProductIdOffset;
        public int ProductRevisionOffset;
        public int SerialNumberOffset;
        public int BusType;
        public int RawPropertiesLength;
        public byte[] RawDeviceProperties = new byte[1];

        public STORAGE_DEVICE_DESCRIPTOR() {
        }

        public STORAGE_DEVICE_DESCRIPTOR(Pointer pointer) {
            super(pointer);
        }

        @Override
        protected List<String> getFieldOrder() {
            return List.of(
                    "Version",
                    "Size",
                    "DeviceType",
                    "DeviceTypeModifier",
                    "RemovableMedia",
                    "CommandQueueing",
                    "VendorIdOffset",
                    "ProductIdOffset",
                    "ProductRevisionOffset",
                    "SerialNumberOffset",
                    "BusType",
                    "RawPropertiesLength",
                    "RawDeviceProperties");
        }

        public static String readNullTerminatedAscii(Memory memory, int offset) {
            if (offset <= 0 || offset >= memory.size()) {
                return "";
            }

            int maxLength = (int) (memory.size() - offset);
            byte[] bytes = memory.getByteArray(offset, maxLength);
            int end = 0;
            while (end < bytes.length && bytes[end] != 0) {
                end++;
            }
            return new String(bytes, 0, end, StandardCharsets.US_ASCII).trim();
        }
    }

    public static class STORAGE_DEVICE_NUMBER extends Structure {
        public int DeviceType;
        public int DeviceNumber;
        public int PartitionNumber;

        public STORAGE_DEVICE_NUMBER() {
        }

        public STORAGE_DEVICE_NUMBER(Pointer pointer) {
            super(pointer);
        }

        @Override
        protected List<String> getFieldOrder() {
            return List.of("DeviceType", "DeviceNumber", "PartitionNumber");
        }
    }
}