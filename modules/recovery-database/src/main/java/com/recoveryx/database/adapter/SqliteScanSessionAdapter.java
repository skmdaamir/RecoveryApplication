package com.recoveryx.database.adapter;

import com.recoveryx.common.enumtype.ScanMode;
import com.recoveryx.common.enumtype.SessionState;
import com.recoveryx.common.util.ValidationUtils;
import com.recoveryx.core.domain.scan.ScanRequest;
import com.recoveryx.core.domain.scan.ScanSession;
import com.recoveryx.core.port.scan.ScanSessionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * SQLite implementation of ScanSessionPort for persisting scan sessions.
 */
@Repository
public class SqliteScanSessionAdapter implements ScanSessionPort {

    private static final Logger log = LoggerFactory.getLogger(SqliteScanSessionAdapter.class);

    private final JdbcTemplate jdbcTemplate;

    public SqliteScanSessionAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = ValidationUtils.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    @Override
    public void save(ScanSession scanSession) {
        ValidationUtils.requireNonNull(scanSession, "scanSession");

        String sql = """
            INSERT INTO scan_sessions (session_id, request_id, device_id, volume_id, scan_mode, state, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(session_id) DO UPDATE SET
                state = excluded.state,
                updated_at = excluded.updated_at
        """;

        ScanRequest request = scanSession.scanRequest();
        String primaryMode = request.scanModes().isEmpty() ? "QUICK_SCAN" : request.scanModes().iterator().next().name();

        jdbcTemplate.update(sql,
                scanSession.sessionId(),
                request.scanId(),
                request.deviceId(),
                request.volumeId() != null ? request.volumeId() : "",
                primaryMode,
                scanSession.state().name(),
                scanSession.createdAt().toString(),
                scanSession.updatedAt().toString()
        );

        log.debug("Saved scan session [{}] to SQLite database", scanSession.sessionId());
    }

    @Override
    public Optional<ScanSession> findById(String sessionId) {
        ValidationUtils.requireNotBlank(sessionId, "sessionId");

        String sql = "SELECT * FROM scan_sessions WHERE session_id = ?";
        List<ScanSession> list = jdbcTemplate.query(sql, sessionRowMapper, sessionId);

        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private static final RowMapper<ScanSession> sessionRowMapper = (rs, rowNum) -> {
        String sessionId = rs.getString("session_id");
        String requestId = rs.getString("request_id");
        String deviceId = rs.getString("device_id");
        String volumeId = rs.getString("volume_id");
        String scanModeStr = rs.getString("scan_mode");
        String stateStr = rs.getString("state");
        Instant createdAt = Instant.parse(rs.getString("created_at"));
        Instant updatedAt = Instant.parse(rs.getString("updated_at"));

        ScanMode mode;
        try {
            mode = ScanMode.valueOf(scanModeStr);
        } catch (Exception e) {
            mode = ScanMode.QUICK_SCAN;
        }

        ScanRequest request = new ScanRequest(
                requestId,
                deviceId,
                volumeId,
                Set.of(mode),
                true,
                true,
                true,
                4,
                1024 * 1024
        );

        SessionState state;
        try {
            state = SessionState.valueOf(stateStr);
        } catch (Exception e) {
            state = SessionState.COMPLETED;
        }

        return new ScanSession(sessionId, request, state, createdAt, updatedAt);
    };
}
