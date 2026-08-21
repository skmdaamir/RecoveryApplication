package com.recoveryx.report.service;

import com.recoveryx.common.util.ValidationUtils;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.core.port.report.ReportExportPort;
import com.recoveryx.database.repository.SqliteDiscoveredFileRepository;
import com.recoveryx.report.exporter.CsvReportExporter;
import com.recoveryx.report.exporter.HtmlReportExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Implementation of ReportExportPort for exporting HTML and CSV reports.
 */
@Service
public class DefaultReportExportPort implements ReportExportPort {

    private static final Logger log = LoggerFactory.getLogger(DefaultReportExportPort.class);

    private final SqliteDiscoveredFileRepository fileRepository;

    public DefaultReportExportPort(SqliteDiscoveredFileRepository fileRepository) {
        this.fileRepository = ValidationUtils.requireNonNull(fileRepository, "fileRepository");
    }

    @Override
    public String export(String sourceId, String format) {
        ValidationUtils.requireNotBlank(sourceId, "sourceId");

        List<RecoverableFile> files = fileRepository.findBySessionId(sourceId);

        String fmt = format != null ? format.toLowerCase().trim() : "html";
        String userHome = System.getProperty("user.home", ".");
        File reportDir = new File(userHome, ".recoveryx/reports");
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }

        try {
            if ("csv".equals(fmt)) {
                Path csvPath = new File(reportDir, "report_" + sourceId + ".csv").toPath();
                CsvReportExporter.exportCsvReport(files, csvPath);
                log.info("Exported CSV report to {}", csvPath);
                return csvPath.toString();
            } else {
                Path htmlPath = new File(reportDir, "report_" + sourceId + ".html").toPath();
                HtmlReportExporter.exportHtmlReport(sourceId, files, htmlPath);
                log.info("Exported HTML report to {}", htmlPath);
                return htmlPath.toString();
            }
        } catch (Exception e) {
            log.error("Failed to export report for source {}: {}", sourceId, e.getMessage(), e);
            throw new RuntimeException("Report export failed: " + e.getMessage(), e);
        }
    }
}
