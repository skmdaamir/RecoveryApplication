package com.recoveryx.nativeaccess.model;

/**
 * Basic storage descriptor information obtained from Windows storage APIs.
 *
 * @param vendor vendor identifier
 * @param product product identifier
 * @param revision product revision
 * @param serialNumber serial number
 * @param removable removable media flag
 * @param busType bus type code
 */
public record NativeStorageDeviceDescriptor(
        String vendor,
        String product,
        String revision,
        String serialNumber,
        boolean removable,
        int busType) {
}