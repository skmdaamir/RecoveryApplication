package com.recoveryx.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/**
 * String normalization helpers.
 */
public final class StringNormalizer {

    private StringNormalizer() {
    }

    public static String normalize(String value) {
        Objects.requireNonNull(value, "value must not be null");
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFKC);
    }

    public static String normalizeLower(String value) {
        return normalize(value).toLowerCase(Locale.ROOT);
    }
}