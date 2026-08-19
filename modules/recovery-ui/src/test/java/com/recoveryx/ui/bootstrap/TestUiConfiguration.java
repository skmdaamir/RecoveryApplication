package com.recoveryx.ui.bootstrap;

import com.recoveryx.ui.config.RecoveryAppProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(RecoveryAppProperties.class)
@Import({
    //Import only non-UI services/ config you want to test
    //eg. some core services if needed
})

public class TestUiConfiguration{
    //No ApplicationShell, no PrimaryStageInitializer, no FXML loaders.
}