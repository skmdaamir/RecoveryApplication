package com.recoveryx.database.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;

/**
 * Spring configuration for embedded SQLite database connection and schema initialization.
 */
@Configuration
public class DatabaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Bean
    public DataSource dataSource() {
        String userHome = System.getProperty("user.home", ".");
        File dbDir = new File(userHome, ".recoveryx");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        File dbFile = new File(dbDir, "recoveryx_sessions.db");
        String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath().replace('\\', '/');

        log.info("Initializing SQLite DataSource at: {}", jdbcUrl);

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(jdbcUrl);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        initializeSchema(jdbcTemplate);
        return jdbcTemplate;
    }

    private static void initializeSchema(JdbcTemplate jdbcTemplate) {
        log.info("Verifying and creating SQLite tables...");

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS scan_sessions (
                session_id TEXT PRIMARY KEY,
                request_id TEXT NOT NULL,
                device_id TEXT NOT NULL,
                volume_id TEXT,
                scan_mode TEXT NOT NULL,
                state TEXT NOT NULL,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            );
        """);

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
                duplicate INTEGER NOT NULL,
                FOREIGN KEY (session_id) REFERENCES scan_sessions(session_id)
            );
        """);

        log.info("SQLite schema initialized successfully.");
    }
}
