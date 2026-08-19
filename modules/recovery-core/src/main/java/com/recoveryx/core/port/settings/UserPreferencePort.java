package com.recoveryx.core.port.settings;

import java.util.Optional;

/**
 * Accesses persisted user preference values.
 */
public interface UserPreferencePort {

    /**
     * Reads a preference by key.
     *
     * @param key preference key
     * @return optional value
     */
    Optional<String> find(String key);

    /**
     * Stores or updates a preference.
     *
     * @param key preference key
     * @param value preference value
     */
    void save(String key, String value);
}