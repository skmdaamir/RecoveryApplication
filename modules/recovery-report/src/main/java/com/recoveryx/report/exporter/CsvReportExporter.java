package com.recoveryx.report.exporter;

import com.recoveryx.core.domain.file.RecoverableFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Exporter for generating CSV spreadsheets for recovered file inventories.
 */
public final class CsvReportExporter {

    private CsvReportExporter() {
    }

    public static Path exportCsvReport(List<RecoverableFile> files, Path outputPath) throws IOException {
        StringBuilder csv = new StringBuilder();
        csv.append("File ID,Name,Extension,Category,Original Path,Size (Bytes),Health Status,Recovery Chance\n");

        for (RecoverableFile f : files) {
            csv.append(escapeCsv(f.fileId())).append(",")
                    .append(escapeCsv(f.name())).append(",")
                    .append(escapeCsv(f.extension())).append(",")
                    .append(escapeCsv(f.category().name())).append(",")
                    .append(escapeCsv(f.originalPath() != null ? f.originalPath() : "")).append(",")
                    .append(f.fileSize()).append(",")
                    .append(f.healthStatus().name()).append(",")
                    .append(f.recoveryChance().name()).append("\n");
        }

        Files.writeString(outputPath, csv.toString(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        return outputPath;
    }

    private static String escapeCsv(String value) {
        if (value == null) return "\"\"";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
