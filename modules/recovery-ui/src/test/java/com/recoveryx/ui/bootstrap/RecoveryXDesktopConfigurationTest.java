package com.recoveryx.ui.bootstrap;

import com.recoveryx.ui.config.RecoveryAppProperties;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "recoveryx.ui.enable-shell=false")
@Import(TestUiConfiguration.class)
@ActiveProfiles("test")
class RecoveryXDesktopConfigurationTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    @Import(RecoveryAppProperties.class)
    static class TestConfig {
    }

    @Test
    void shouldStartDesktopSpringContext() {
        assertTrue(true);
    }
}