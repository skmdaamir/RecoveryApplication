@echo off
echo Generating internal packages and Java files...

:: ========================================
:: MODULE: recovery-common
:: ========================================
set "COM_BASE=modules\recovery-common\src\main\java\com\recoveryx\common"

echo Creating common/constant...
mkdir "%COM_BASE%\constant" 2>nul
type nul > "%COM_BASE%\constant\RecoveryConstants.java"

echo Creating common/enumtype...
mkdir "%COM_BASE%\enumtype" 2>nul
type nul > "%COM_BASE%\enumtype\ApplicationTheme.java"
type nul > "%COM_BASE%\enumtype\ChecksumAlgorithm.java"
type nul > "%COM_BASE%\enumtype\DeviceBusType.java"
type nul > "%COM_BASE%\enumtype\DeviceType.java"
type nul > "%COM_BASE%\enumtype\FileCategory.java"
type nul > "%COM_BASE%\enumtype\FileSystemType.java"
type nul > "%COM_BASE%\enumtype\HealthStatus.java"
type nul > "%COM_BASE%\enumtype\LanguageCode.java"
type nul > "%COM_BASE%\enumtype\LogCategory.java"
type nul > "%COM_BASE%\enumtype\RecoveryChance.java"
type nul > "%COM_BASE%\enumtype\ScanMode.java"
type nul > "%COM_BASE%\enumtype\ScanState.java"
type nul > "%COM_BASE%\enumtype\SessionState.java"

echo Creating common/exception...
mkdir "%COM_BASE%\exception" 2>nul
type nul > "%COM_BASE%\exception\RecoveryXException.java"
type nul > "%COM_BASE%\exception\ConfigurationException.java"
type nul > "%COM_BASE%\exception\DeviceAccessException.java"
type nul > "%COM_BASE%\exception\ParsingException.java"
type nul > "%COM_BASE%\exception\RecoveryOperationException.java"
type nul > "%COM_BASE%\exception\ValidationException.java"

echo Creating common/result...
mkdir "%COM_BASE%\result" 2>nul
type nul > "%COM_BASE%\result\OperationError.java"
type nul > "%COM_BASE%\result\OperationResult.java"
type nul > "%COM_BASE%\result\PageResult.java"

echo Creating common/util...
mkdir "%COM_BASE%\util" 2>nul
type nul > "%COM_BASE%\util\ChecksumUtils.java"
type nul > "%COM_BASE%\util\CollectionUtils.java"
type nul > "%COM_BASE%\util\IdGenerator.java"
type nul > "%COM_BASE%\util\InstantUtils.java"
type nul > "%COM_BASE%\util\SizeFormatter.java"
type nul > "%COM_BASE%\util\StringNormalizer.java"
type nul > "%COM_BASE%\util\ValidationUtils.java"


:: ========================================
:: MODULE: recovery-core
:: ========================================
set "COR_BASE=modules\recovery-core\src\main\java\com\recoveryx\core"

echo Creating core/domain...
mkdir "%COR_BASE%\domain\device" 2>nul
type nul > "%COR_BASE%\domain\device\DeviceDescriptor.java"
type nul > "%COR_BASE%\domain\device\DeviceGeometry.java"
type nul > "%COR_BASE%\domain\device\DeviceHealthSnapshot.java"
type nul > "%COR_BASE%\domain\device\VolumeDescriptor.java"

mkdir "%COR_BASE%\domain\file" 2>nul
type nul > "%COR_BASE%\domain\file\FileFragment.java"
type nul > "%COR_BASE%\domain\file\FileSignatureMatch.java"
type nul > "%COR_BASE%\domain\file\RecoverableFile.java"

mkdir "%COR_BASE%\domain\recovery" 2>nul
type nul > "%COR_BASE%\domain\recovery\RecoveryRequest.java"
type nul > "%COR_BASE%\domain\recovery\RecoveryResult.java"
type nul > "%COR_BASE%\domain\recovery\RecoveryTarget.java"

mkdir "%COR_BASE%\domain\scan" 2>nul
type nul > "%COR_BASE%\domain\scan\ScanFilter.java"
type nul > "%COR_BASE%\domain\scan\ScanProgress.java"
type nul > "%COR_BASE%\domain\scan\ScanRequest.java"
type nul > "%COR_BASE%\domain\scan\ScanResultSummary.java"
type nul > "%COR_BASE%\domain\scan\ScanSession.java"

echo Creating core/port...
mkdir "%COR_BASE%\port\device" 2>nul
type nul > "%COR_BASE%\port\device\DeviceDiscoveryPort.java"
type nul > "%COR_BASE%\port\device\DeviceInspectionPort.java"

mkdir "%COR_BASE%\port\recovery" 2>nul
type nul > "%COR_BASE%\port\recovery\RecoveryOrchestratorPort.java"

mkdir "%COR_BASE%\port\report" 2>nul
type nul > "%COR_BASE%\port\report\ReportExportPort.java"

mkdir "%COR_BASE%\port\scan" 2>nul
type nul > "%COR_BASE%\port\scan\ScanExecutionPort.java"
type nul > "%COR_BASE%\port\scan\ScanSessionPort.java"

mkdir "%COR_BASE%\port\settings" 2>nul
type nul > "%COR_BASE%\port\settings\UserPreferencePort.java"

echo Creating core/usecase...
mkdir "%COR_BASE%\usecase" 2>nul
type nul > "%COR_BASE%\usecase\DiscoverDevicesUseCase.java"
type nul > "%COR_BASE%\usecase\RecoverFilesUseCase.java"
type nul > "%COR_BASE%\usecase\ResumeSessionUseCase.java"
type nul > "%COR_BASE%\usecase\StartScanUseCase.java"
type nul > "%COR_BASE%\usecase\StopScanUseCase.java"


:: ========================================
:: MODULE: recovery-ui
:: ========================================
set "UI_BASE=modules\recovery-ui\src\main\java\com\recoveryx\ui"

echo Creating ui/config...
mkdir "%UI_BASE%\config" 2>nul
type nul > "%UI_BASE%\config\RecoveryAppProperties.java"
type nul > "%UI_BASE%\config\RecoveryAppPropertiesRegistrar.java"

echo.
echo =========================================
echo All packages and Java files created!
echo =========================================
goto :EOF