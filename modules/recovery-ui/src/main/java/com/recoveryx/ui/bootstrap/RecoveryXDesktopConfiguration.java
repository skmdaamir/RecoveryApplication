package com.recoveryx.ui.bootstrap;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot configuration root for the RecoveryX Pro desktop application.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "com.recoveryx")
public class RecoveryXDesktopConfiguration {
}