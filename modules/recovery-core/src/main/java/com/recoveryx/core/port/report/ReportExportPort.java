package com.recoveryx.core.port.report;

/**
 * Exports reports for scans and recovery results.
 */
public interface ReportExportPort {

    /**
     * Exports a report.
     *
     * @param sourceId scan or recovery source identifier
     * @param format   target report format
     * @return generated report path
     */
    String export(String sourceId, String format);
}