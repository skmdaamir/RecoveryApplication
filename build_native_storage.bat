@echo off
echo Generating packages and Java files for native and storage modules...

:: ========================================
:: MODULE: recovery-native (main)
:: ========================================
set "NAT_MAIN=modules\recovery-native\src\main\java\com\recoveryx\nativeaccess"

echo Creating nativeaccess/config...
mkdir "%NAT_MAIN%\config" 2>nul
type nul > "%NAT_MAIN%\config\NativeAccessConfiguration.java"

echo Creating nativeaccess/constant...
mkdir "%NAT_MAIN%\constant" 2>nul
type nul > "%NAT_MAIN%\constant\WinIoctlConstants.java"

echo Creating nativeaccess/jna...
mkdir "%NAT_MAIN%\jna" 2>nul
type nul > "%NAT_MAIN%\jna\Kernel32Ex.java"
type nul > "%NAT_MAIN%\jna\SetupApiEx.java"
type nul > "%NAT_MAIN%\jna\WinBaseEx.java"
type nul > "%NAT_MAIN%\jna\WinIoctlStructures.java"
type nul > "%NAT_MAIN%\jna\WinNtEx.java"

echo Creating nativeaccess/model...
mkdir "%NAT_MAIN%\model" 2>nul
type nul > "%NAT_MAIN%\model\NativeDeviceHandle.java"
type nul > "%NAT_MAIN%\model\NativeDriveGeometry.java"
type nul > "%NAT_MAIN%\model\NativeStorageDeviceDescriptor.java"
type nul > "%NAT_MAIN%\model\RawReadRequest.java"

echo Creating nativeaccess/service...
mkdir "%NAT_MAIN%\service" 2>nul
type nul > "%NAT_MAIN%\service\WindowsDeviceDiscoveryService.java"
type nul > "%NAT_MAIN%\service\WindowsDeviceInspectionService.java"
type nul > "%NAT_MAIN%\service\WindowsNativeDiskAccessService.java"
type nul > "%NAT_MAIN%\service\WindowsPrivilegeService.java"

echo Creating nativeaccess/util...
mkdir "%NAT_MAIN%\util" 2>nul
type nul > "%NAT_MAIN%\util\AlignmentUtils.java"
type nul > "%NAT_MAIN%\util\NativeErrorUtils.java"
type nul > "%NAT_MAIN%\util\WindowsDevicePathFactory.java"

:: ========================================
:: MODULE: recovery-storage (main)
:: ========================================
set "STO_MAIN=modules\recovery-storage\src\main\java\com\recoveryx\storage"

echo Creating storage/adapter...
mkdir "%STO_MAIN%\adapter" 2>nul
type nul > "%STO_MAIN%\adapter\DefaultDeviceDiscoveryAdapter.java"
type nul > "%STO_MAIN%\adapter\DefaultDeviceInspectionAdapter.java"

echo Creating storage/model...
mkdir "%STO_MAIN%\model" 2>nul
type nul > "%STO_MAIN%\model\SectorReadResult.java"

echo Creating storage/service...
mkdir "%STO_MAIN%\service" 2>nul
type nul > "%STO_MAIN%\service\SectorReaderService.java"

:: ========================================
:: MODULE: recovery-native (test)
:: ========================================
set "NAT_TEST=modules\recovery-native\src\test\java\com\recoveryx\nativeaccess"

echo Creating nativeaccess/test/util...
mkdir "%NAT_TEST%\util" 2>nul
type nul > "%NAT_TEST%\util\AlignmentUtilsTest.java"
type nul > "%NAT_TEST%\util\WindowsDevicePathFactoryTest.java"

echo.
echo =========================================
echo Native and Storage files created!
echo =========================================
goto :EOF