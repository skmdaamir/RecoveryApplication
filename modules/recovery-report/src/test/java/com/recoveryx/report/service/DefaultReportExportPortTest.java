package com.recoveryx.report.service;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.common.enumtype.HealthStatus;
import com.recoveryx.common.enumtype.RecoveryChance;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.database.repository.SqliteDiscoveredFileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultReportExportPortTest {

    @Test
    void shouldExportHtmlAndCsvReports() {
        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:file:memdb3?mode=memory&cache=shared");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

        SqliteDiscoveredFileRepository stubRepo = new SqliteDiscoveredFileRepository(jdbcTemplate) {
            @Override
            public List<RecoverableFile> findBySessionId(String sessionId) {
                RecoverableFile file = new RecoverableFile(
                        "f10",
                        "vacation.jpg",
                        "jpg",
                        FileCategory.IMAGE,
                        "/vacation.jpg",
                        null,
                        500000L,
                        Instant.now(),
                        Instant.now(),
                        Instant.now(),
                        HealthStatus.EXCELLENT,
                        RecoveryChance.EXCELLENT,
                        true,
                        false,
                        List.of(),
                        null);
                return List.of(file);
            }
        };

        DefaultReportExportPort service = new DefaultReportExportPort(stubRepo);

        String htmlPathStr = service.export("sess-test", "html");
        assertNotNull(htmlPathStr);
        assertTrue(Files.exists(Path.of(htmlPathStr)));

        String csvPathStr = service.export("sess-test", "csv");
        assertNotNull(csvPathStr);
        assertTrue(Files.exists(Path.of(csvPathStr)));
    }
}
