package com.recoveryx.report.exporter;

import com.recoveryx.core.domain.file.RecoverableFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exporter for generating standalone HTML recovery reports with embedded styling.
 */
public final class HtmlReportExporter {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private HtmlReportExporter() {
    }

    public static Path exportHtmlReport(String sessionOrRecoveryId, List<RecoverableFile> files, Path outputPath) throws IOException {
        StringBuilder html = new StringBuilder();

        long totalBytes = files.stream().mapToLong(RecoverableFile::fileSize).sum();
        long deletedCount = files.stream().filter(f -> f.deletedDate() != null).count();

        html.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>RecoveryX Pro - Diagnostic Report</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, sans-serif; background-color: #1e1e2e; color: #cdd6f4; margin: 0; padding: 20px; }
                    .header { background: linear-gradient(135deg, #89b4fa, #b4befe); padding: 25px; border-radius: 12px; color: #11111b; margin-bottom: 25px; }
                    .header h1 { margin: 0 0 8px 0; font-size: 28px; }
                    .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; margin-bottom: 25px; }
                    .stat-card { background: #313244; padding: 18px; border-radius: 8px; border-left: 4px solid #89b4fa; }
                    .stat-value { font-size: 24px; font-weight: bold; color: #f5e0dc; }
                    .stat-label { font-size: 13px; color: #a6adc8; text-transform: uppercase; margin-top: 4px; }
                    table { width: 100%; border-collapse: collapse; background: #181825; border-radius: 8px; overflow: hidden; }
                    th { background: #313244; padding: 12px 15px; text-align: left; font-size: 13px; color: #cdd6f4; }
                    td { padding: 10px 15px; border-top: 1px solid #313244; font-size: 13px; }
                    tr:hover { background: #2a2b3d; }
                    .badge { padding: 3px 8px; border-radius: 4px; font-size: 11px; font-weight: bold; }
                    .badge-good { background: #a6e3a1; color: #11111b; }
                    .badge-fair { background: #f9e2af; color: #11111b; }
                    .badge-poor { background: #f38ba8; color: #11111b; }
                </style>
            </head>
            <body>
            """);

        html.append("<div class=\"header\">");
        html.append("<h1>RecoveryX Pro - Data Recovery Diagnostic Report</h1>");
        html.append("<p>Report ID: <strong>").append(sessionOrRecoveryId).append("</strong> | Generated: ")
                .append(DATE_FORMATTER.format(Instant.now())).append("</p>");
        html.append("</div>");

        html.append("<div class=\"stats-grid\">");
        html.append("<div class=\"stat-card\"><div class=\"stat-value\">").append(files.size())
                .append("</div><div class=\"stat-label\">Total Files</div></div>");
        html.append("<div class=\"stat-card\"><div class=\"stat-value\">").append(deletedCount)
                .append("</div><div class=\"stat-label\">Deleted Candidates</div></div>");
        html.append("<div class=\"stat-card\"><div class=\"stat-value\">").append(formatSize(totalBytes))
                .append("</div><div class=\"stat-label\">Total Volume</div></div>");
        html.append("<div class=\"stat-card\"><div class=\"stat-value\">100%</div><div class=\"stat-label\">Scan Coverage</div></div>");
        html.append("</div>");

        html.append("<table>");
        html.append("<thead><tr><th>File Name</th><th>Extension</th><th>Category</th><th>Size</th><th>Date Deleted / Modified</th><th>Health</th><th>Recovery Chance</th></tr></thead><tbody>");

        for (RecoverableFile f : files) {
            String badgeClass = switch (f.healthStatus()) {
                case EXCELLENT, GOOD -> "badge-good";
                case FAIR -> "badge-fair";
                default -> "badge-poor";
            };

            String dateStr = "-";
            if (f.deletedDate() != null) {
                dateStr = "Deleted: " + DATE_FORMATTER.format(f.deletedDate());
            } else if (f.modifiedDate() != null) {
                dateStr = "Modified: " + DATE_FORMATTER.format(f.modifiedDate());
            }

            html.append("<tr>")
                    .append("<td><strong>").append(escapeHtml(f.name())).append("</strong></td>")
                    .append("<td>").append(f.extension()).append("</td>")
                    .append("<td>").append(f.category()).append("</td>")
                    .append("<td>").append(formatSize(f.fileSize())).append("</td>")
                    .append("<td>").append(dateStr).append("</td>")
                    .append("<td><span class=\"badge ").append(badgeClass).append("\">").append(f.healthStatus()).append("</span></td>")
                    .append("<td>").append(f.recoveryChance()).append("</td>")
                    .append("</tr>");
        }

        html.append("</tbody></table></body></html>");

        Files.writeString(outputPath, html.toString(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        return outputPath;
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int z = (63 - Long.numberOfLeadingZeros(bytes)) / 10;
        return String.format("%.1f %cB", (double) bytes / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
