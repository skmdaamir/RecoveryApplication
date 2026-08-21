package com.recoveryx.engine.reconstruction;

import com.recoveryx.common.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DestinationSafetyValidatorTest {

    @Test
    void shouldAllowValidTargetDirectoryWhenSourceIsDifferentDrive(@TempDir Path tempDir) {
        // Use a source drive letter different from the tempDir drive root
        String tempRoot = tempDir.getRoot() != null ? tempDir.getRoot().toString().toUpperCase().substring(0, 2) : "C:";
        String sourceDrive = tempRoot.startsWith("C") ? "Z:\\" : "C:\\";

        assertDoesNotThrow(() -> {
            DestinationSafetyValidator.validateSafety(tempDir.toString(), sourceDrive);
        });
    }

    @Test
    void shouldThrowExceptionWhenTargetIsOnSameSourceDrive(@TempDir Path tempDir) {
        String targetPath = tempDir.toString();
        String sameDriveLetter = tempDir.getRoot() != null ? tempDir.getRoot().toString() : "C:\\";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            DestinationSafetyValidator.validateSafety(targetPath, sameDriveLetter);
        });

        assertTrue(ex.getMessage().contains("DANGER"), "Should display safety warning message");
    }

    @Test
    void shouldThrowExceptionForBlankTarget() {
        assertThrows(ValidationException.class, () -> {
            DestinationSafetyValidator.validateSafety("", "E:\\");
        });
    }
}
