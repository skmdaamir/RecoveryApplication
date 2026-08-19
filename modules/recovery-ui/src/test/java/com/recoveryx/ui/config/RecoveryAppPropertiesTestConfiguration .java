package com.recoveryx.ui.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RecoveryAppProperties.class)
@ComponentScan(
        basePackages = "com.recoveryx.ui.config",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = ApplicationShell.class
        )
public class RecoveryAppPropertiesTestConfiguration {
}