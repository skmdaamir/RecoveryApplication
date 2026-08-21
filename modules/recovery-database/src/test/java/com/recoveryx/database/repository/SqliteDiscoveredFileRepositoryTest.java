package com.recoveryx.database.repository;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.common.enumtype.HealthStatus;
import com.recoveryx.common.enumtype.RecoveryChance;
import com.recoveryx.core.domain.file.RecoverableFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqliteDiscoveredFileRepositoryTest {

    private SqliteDiscoveredFileRepository repository;

    @BeforeEach
    void setUp() {
        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:file:memdb2?mode=memory&cache=shared");
        ds.setSuppressClose(true);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS discovered_files (
                file_id TEXT PRIMARY KEY,
                session_id TEXT NOT NULL,
                name TEXT NOT NULL,
                extension TEXT NOT NULL,
                category TEXT NOT NULL,
                original_path TEXT,
                file_size INTEGER NOT NULL,
                deleted_date TEXT,
                created_date TEXT,
                modified_date TEXT,
                health_status TEXT NOT NULL,
                recovery_chance TEXT NOT NULL,
                preview_available INTEGER NOT NULL,
                duplicate INTEGER NOT NULL
            );
        """);

        repository = new SqliteDiscoveredFileRepository(jdbcTemplate);
    }

    @Test
    void shouldSaveAllAndFindBySessionId() {
        RecoverableFile file1 = new RecoverableFile(
                "f1",
                "photo.jpg",
                "jpg",
                FileCategory.IMAGE,
                "/photo.jpg",
                null,
                1024L,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                HealthStatus.EXCELLENT,
                RecoveryChance.EXCELLENT,
                true,
                false,
                List.of(),
                null);

        RecoverableFile file2 = new RecoverableFile(
                "f2",
                "doc.pdf",
                "pdf",
                FileCategory.PDF,
                "/doc.pdf",
                null,
                2048L,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                HealthStatus.GOOD,
                RecoveryChance.HIGH,
                true,
                false,
                List.of(),
                null);

        repository.saveAll("sess-1", List.of(file1, file2));

        List<RecoverableFile> results = repository.findBySessionId("sess-1");
        assertEquals(2, results.size());

        List<RecoverableFile> images = repository.findBySessionIdAndCategory("sess-1", FileCategory.IMAGE);
        assertEquals(1, images.size());
        assertEquals("photo.jpg", images.get(0).name());
    }
}
