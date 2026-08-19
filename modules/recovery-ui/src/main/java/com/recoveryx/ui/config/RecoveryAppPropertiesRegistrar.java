package com.recoveryx.ui.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers validated application configuration properties.
 */
@Configuration
@EnableConfigurationProperties(RecoveryAppProperties.class)
public class RecoveryAppPropertiesRegistrar {
}