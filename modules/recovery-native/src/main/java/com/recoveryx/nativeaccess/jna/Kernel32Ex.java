package com.recoveryx.nativeaccess.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinDef.DWORD;

/**
 * Extended Kernel32 bindings required for direct disk access.
 */
public interface Kernel32Ex extends StdCallLibrary {

    Kernel32Ex INSTANCE = Native.load("kernel32", Kernel32Ex.class);

    WinNT.HANDLE CreateFileW(
            char[] lpFileName,
            int dwDesiredAccess,
            int dwShareMode,
            Pointer lpSecurityAttributes,
            int dwCreationDisposition,
            int dwFlagsAndAttributes,
            WinNT.HANDLE hTemplateFile
    );

    boolean ReadFile(
            WinNT.HANDLE hFile,
            Pointer lpBuffer,
            int nNumberOfBytesToRead,
            IntByReference lpNumberOfBytesRead,
            WinBase.OVERLAPPED lpOverlapped
    );

    boolean SetFilePointerEx(
            WinNT.HANDLE hFile,
            long liDistanceToMove,
            LongByReferenceEx lpNewFilePointer,
            int dwMoveMethod
    );

    boolean DeviceIoControl(
            WinNT.HANDLE hDevice,
            int dwIoControlCode,
            Pointer lpInBuffer,
            int nInBufferSize,
            Pointer lpOutBuffer,
            int nOutBufferSize,
            IntByReference lpBytesReturned,
            WinBase.OVERLAPPED lpOverlapped
    );

    boolean CloseHandle(WinNT.HANDLE hObject);

    int GetLastError();

    WinNT.HANDLE FindFirstVolumeW(char[] lpszVolumeName, int cchBufferLength);

    boolean FindNextVolumeW(WinNT.HANDLE hFindVolume, char[] lpszVolumeName, int cchBufferLength);

    boolean FindVolumeClose(WinNT.HANDLE hFindVolume);

    int QueryDosDeviceW(String lpDeviceName, char[] lpTargetPath, int ucchMax);

    boolean GetVolumePathNamesForVolumeNameW(
            char[] lpszVolumeName,
            char[] lpszVolumePathNames,
            int cchBufferLength,
            IntByReference lpcchReturnLength);
    /**
     * Long pointer reference for 64-bit file pointer results.
     */
    class LongByReferenceEx extends Structure {
        public long value;

        @Override
        protected java.util.List<String> getFieldOrder() {
            return java.util.List.of("value");
        }
    }
}