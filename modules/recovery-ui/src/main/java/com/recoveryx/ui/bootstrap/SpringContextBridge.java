package com.recoveryx.ui.bootstrap;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Creates and manages the Spring application context for desktop mode.
 */
public final class SpringContextBridge {

    private SpringContextBridge() {
    }

    public static ConfigurableApplicationContext start(String[] args) {
        return new SpringApplicationBuilder(RecoveryXDesktopConfiguration.class)
                .headless(false)
                .web(WebApplicationType.NONE)
                .logStartupInfo(true)
                .run(args);
    }
}