package com.recoveryx.ui.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecoveryAppPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ConfigurationPropertiesAutoConfiguration.class,
                    ValidationAutoConfiguration.class))
            .withUserConfiguration(RecoveryAppPropertiesRegistrar.class);

    @Test
    void shouldBindValidProperties() {
        contextRunner
                .withPropertyValues(
                        "recoveryx.application-name=RecoveryX Pro",
                        "recoveryx.ui.theme=dark"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(RecoveryAppProperties.class);

                    RecoveryAppProperties properties =
                            context.getBean(RecoveryAppProperties.class);

                    assertThat(properties.getApplicationName())
                            .isEqualTo("RecoveryX Pro");
                    assertThat(properties.getUi().getTheme())
                            .isEqualTo("dark");
                });
    }

    @Test
    void shouldRejectInvalidProperties() {
        contextRunner
                .withPropertyValues(
                        "recoveryx.app.scan-threads=0",
                        "recoveryx.app.buffer-size-bytes=1",
                        "recoveryx.app.recovery-directory=")
                .run(context -> {
                    Throwable failure = context.getStartupFailure();
                    assertNotNull(failure);
                    assertTrue(failure.getMessage().contains("Binding") || failure.getMessage().contains("validation") || failure.getMessage().contains("failed") || failure.getMessage().contains("scan-threads") || failure.getCause() != null);
                });
    }
}