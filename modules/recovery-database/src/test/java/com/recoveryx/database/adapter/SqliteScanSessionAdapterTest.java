package com.recoveryx.database.adapter;

import com.recoveryx.common.enumtype.ScanMode;
import com.recoveryx.common.enumtype.SessionState;
import com.recoveryx.core.domain.scan.ScanRequest;
import com.recoveryx.core.domain.scan.ScanSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SqliteScanSessionAdapterTest {

    private SqliteScanSessionAdapter adapter;

    @BeforeEach
    void setUp() {
        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:file:memdb1?mode=memory&cache=shared");
        ds.setSuppressClose(true);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

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

        adapter = new SqliteScanSessionAdapter(jdbcTemplate);
    }

    @Test
    void shouldSaveAndFindSessionById() {
        ScanRequest request = new ScanRequest(
                "req-001",
                "device-001",
                "vol-001",
                Set.of(ScanMode.QUICK_SCAN),
                true,
                true,
                true,
                4,
                1024 * 1024
        );

        ScanSession session = new ScanSession(
                "session-001",
                request,
                SessionState.ACTIVE,
                Instant.now(),
                Instant.now()
        );

        adapter.save(session);

        Optional<ScanSession> found = adapter.findById("session-001");
        assertTrue(found.isPresent(), "Session should be found in memory SQLite database");
        assertEquals("session-001", found.get().sessionId());
        assertEquals(SessionState.ACTIVE, found.get().state());
    }
}
