package com.recoveryx.database.repository;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.common.enumtype.HealthStatus;
import com.recoveryx.common.enumtype.RecoveryChance;
import com.recoveryx.common.util.ValidationUtils;
import com.recoveryx.core.domain.file.RecoverableFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite repository for persisting and querying discovered RecoverableFile candidates.
 */
@Repository
public class SqliteDiscoveredFileRepository {

    private static final Logger log = LoggerFactory.getLogger(SqliteDiscoveredFileRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public SqliteDiscoveredFileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = ValidationUtils.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    /**
     * Batch saves discovered files under a session ID.
     */
    public void saveAll(String sessionId, List<RecoverableFile> files) {
        ValidationUtils.requireNotBlank(sessionId, "sessionId");
        if (files == null || files.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO discovered_files (
                file_id, session_id, name, extension, category, original_path,
                file_size, deleted_date, created_date, modified_date, health_status,
                recovery_chance, preview_available, duplicate
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(file_id) DO NOTHING
        """;

        List<Object[]> batchArgs = new ArrayList<>();
        for (RecoverableFile f : files) {
            batchArgs.add(new Object[]{
                    f.fileId(),
                    sessionId,
                    f.name(),
                    f.extension(),
                    f.category().name(),
                    f.originalPath(),
                    f.fileSize(),
                    f.deletedDate() != null ? f.deletedDate().toString() : null,
                    f.createdDate() != null ? f.createdDate().toString() : null,
                    f.modifiedDate() != null ? f.modifiedDate().toString() : null,
                    f.healthStatus().name(),
                    f.recoveryChance().name(),
                    f.previewAvailable() ? 1 : 0,
                    f.duplicate() ? 1 : 0
            });
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
        log.info("Persisted {} discovered files for session [{}] to SQLite", files.size(), sessionId);
    }

    /**
     * Retrieves all discovered files for a session.
     */
    public List<RecoverableFile> findBySessionId(String sessionId) {
        ValidationUtils.requireNotBlank(sessionId, "sessionId");
        String sql = "SELECT * FROM discovered_files WHERE session_id = ?";
        return jdbcTemplate.query(sql, fileRowMapper, sessionId);
    }

    /**
     * Queries files by session ID and category.
     */
    public List<RecoverableFile> findBySessionIdAndCategory(String sessionId, FileCategory category) {
        ValidationUtils.requireNotBlank(sessionId, "sessionId");
        ValidationUtils.requireNonNull(category, "category");
        String sql = "SELECT * FROM discovered_files WHERE session_id = ? AND category = ?";
        return jdbcTemplate.query(sql, fileRowMapper, sessionId, category.name());
    }

    private static final RowMapper<RecoverableFile> fileRowMapper = (rs, rowNum) -> {
        String fileId = rs.getString("file_id");
        String name = rs.getString("name");
        String extension = rs.getString("extension");
        String categoryStr = rs.getString("category");
        String originalPath = rs.getString("original_path");
        long fileSize = rs.getLong("file_size");

        String deletedStr = rs.getString("deleted_date");
        String createdStr = rs.getString("created_date");
        String modifiedStr = rs.getString("modified_date");

        String healthStr = rs.getString("health_status");
        String chanceStr = rs.getString("recovery_chance");
        boolean previewAvailable = rs.getInt("preview_available") == 1;
        boolean duplicate = rs.getInt("duplicate") == 1;

        FileCategory category = parseEnum(FileCategory.class, categoryStr, FileCategory.UNKNOWN);
        HealthStatus health = parseEnum(HealthStatus.class, healthStr, HealthStatus.GOOD);
        RecoveryChance chance = parseEnum(RecoveryChance.class, chanceStr, RecoveryChance.HIGH);

        return new RecoverableFile(
                fileId,
                name,
                extension,
                category,
                originalPath,
                null,
                fileSize,
                deletedStr != null ? Instant.parse(deletedStr) : null,
                createdStr != null ? Instant.parse(createdStr) : null,
                modifiedStr != null ? Instant.parse(modifiedStr) : null,
                health,
                chance,
                previewAvailable,
                duplicate,
                List.of(), // Fragments can be loaded on demand
                null
        );
    };

    private static <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, E fallback) {
        try {
            return Enum.valueOf(enumClass, value);
        } catch (Exception e) {
            return fallback;
        }
    }
}
