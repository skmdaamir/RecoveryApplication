package com.recoveryx.filesystem.service;

import com.recoveryx.common.enumtype.FileSystemType;
import com.recoveryx.core.domain.file.RecoverableFile;

import java.util.List;
import java.util.function.Consumer;

/**
 * Strategy interface for parsing a specific filesystem format.
 */
public interface FileSystemParser {

    FileSystemType getSupportedFileSystemType();

    List<RecoverableFile> parseVolume(
            String devicePath,
            long partitionStartSector,
            long partitionSectorCount,
            int bytesPerSector,
            Consumer<RecoverableFile> fileConsumer);
}
