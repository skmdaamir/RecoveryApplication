package com.recoveryx.common.util;

import java.util.UUID;

/**
 * Generates unique identifiers for sessions, jobs, and domain entities.
 */
public final class IdGenerator {

    private IdGenerator() {
    }

    public static String newId() {
        return UUID.randomUUID().toString();
    }
}