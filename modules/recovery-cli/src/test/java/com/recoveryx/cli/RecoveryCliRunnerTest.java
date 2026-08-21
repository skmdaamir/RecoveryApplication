package com.recoveryx.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RecoveryCliRunnerTest {

    private final RecoveryCliRunner runner = new RecoveryCliRunner();

    @Test
    void shouldRunWithoutArgs() {
        assertDoesNotThrow(() -> runner.run());
    }

    @Test
    void shouldRunWithDeviceArgs(@TempDir Path tempDir) {
        assertDoesNotThrow(() -> runner.run(
                "--device", "D:\\",
                "--mode", "QUICK",
                "--output", tempDir.toString()
        ));
    }
}
