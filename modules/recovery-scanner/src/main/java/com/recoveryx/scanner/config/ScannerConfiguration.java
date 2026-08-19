package com.recoveryx.scanner.config;

import com.recoveryx.filesystem.service.FileSystemService;
import com.recoveryx.storage.service.SectorReaderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for scanner module services.
 */
@Configuration
public class ScannerConfiguration {

    @Bean
    public FileSystemService fileSystemService(SectorReaderService sectorReaderService) {
        return new FileSystemService(sectorReaderService);
    }
}
