package com.recoveryx.storage.config;

import com.recoveryx.nativeaccess.service.WindowsNativeDiskAccessService;
import com.recoveryx.storage.service.RawSectorReader;
import com.recoveryx.storage.service.SectorCache;
import com.recoveryx.storage.service.SectorReaderService;
import com.recoveryx.storage.service.StorageDeviceService;
import com.recoveryx.storage.service.impl.DefaultSectorCache;
import com.recoveryx.storage.service.impl.WindowsRawSectorReader;
import com.recoveryx.storage.service.impl.WindowsStorageDeviceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for storage services.
 */
@Configuration
public class StorageConfiguration {

    @Bean
    public SectorCache sectorCache() {
        return new DefaultSectorCache(256);
    }

    @Bean
    public RawSectorReader rawSectorReader(WindowsNativeDiskAccessService nativeDiskAccessService,
            SectorCache sectorCache) {
        return new WindowsRawSectorReader(nativeDiskAccessService, sectorCache);
    }

    @Bean
    public StorageDeviceService storageDeviceService(WindowsNativeDiskAccessService nativeDiskAccessService) {
        return new WindowsStorageDeviceService(nativeDiskAccessService);
    }

    @Bean
    public SectorReaderService sectorReaderService(RawSectorReader rawSectorReader) {
        return new SectorReaderService(rawSectorReader);
    }
}