package com.recoveryx.filesystem.service;

import com.recoveryx.common.enumtype.FileSystemType;
import com.recoveryx.common.util.ValidationUtils;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry holding filesystem parser strategies.
 */
public final class FileSystemParserRegistry {

    private final Map<FileSystemType, FileSystemParser> parserMap = new EnumMap<>(FileSystemType.class);

    public FileSystemParserRegistry(List<FileSystemParser> parsers) {
        if (parsers != null) {
            for (FileSystemParser parser : parsers) {
                register(parser);
            }
        }
    }

    public void register(FileSystemParser parser) {
        ValidationUtils.requireNonNull(parser, "parser");
        parserMap.put(parser.getSupportedFileSystemType(), parser);
    }

    public Optional<FileSystemParser> getParser(FileSystemType type) {
        return Optional.ofNullable(parserMap.get(type));
    }
}
