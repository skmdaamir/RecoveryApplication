package com.recoveryx.nativeaccess.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WindowsDevicePathFactoryTest {

    @Test
    void shouldBuildPhysicalDrivePath() {
        assertEquals("\\\\.\\PhysicalDrive0", WindowsDevicePathFactory.physicalDrivePath(0));
        assertEquals("\\\\.\\PhysicalDrive5", WindowsDevicePathFactory.physicalDrivePath(5));
    }

    @Test
    void shouldBuildVolumePath() {
        assertEquals("\\\\.\\C:", WindowsDevicePathFactory.volumePath("c"));
        assertEquals("\\\\.\\Z:", WindowsDevicePathFactory.volumePath("Z"));
    }

    @Test
    void shouldRejectInvalidVolumePath() {
        assertThrows(IllegalArgumentException.class, () -> WindowsDevicePathFactory.volumePath("AA"));
    }
}