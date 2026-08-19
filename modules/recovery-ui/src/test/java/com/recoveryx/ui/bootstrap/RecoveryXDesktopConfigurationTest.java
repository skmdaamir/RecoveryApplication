package com.recoveryx.ui.bootstrap;

import com.recoveryx.ui.config.RecoveryAppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties="recoveryx.ui.enable-shell=false")
@Import(TestUiConfiguration.class) //use a test-only config
@ActiveProfiles("test")
class RecoveryXDesktopConfigurationTest {

    @Import(RecoveryAppProperties.class)
    static class TestConfig {
        //only config/properties, no UI beans
    }

    @Test
    void shouldStartDesktopSpringContext() {
        assertTrue(true);
    }
}