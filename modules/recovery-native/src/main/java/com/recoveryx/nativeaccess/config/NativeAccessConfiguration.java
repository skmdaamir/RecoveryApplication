package com.recoveryx.nativeaccess.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Registers Windows native access services and adapters.
 */
@Configuration
@ComponentScan(basePackages = {
        "com.recoveryx.nativeaccess",
        "com.recoveryx.storage"
})
public class NativeAccessConfiguration {
}