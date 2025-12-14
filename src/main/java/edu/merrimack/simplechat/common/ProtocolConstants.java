package edu.merrimack.simplechat.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Central place for protocol-wide constants and limits.
 */
public final class ProtocolConstants {
    public static final String VERSION = "1.0";
    public static final Charset UTF8 = StandardCharsets.UTF_8;
    public static final int MAX_USERNAME_LENGTH = 32;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_CONTENT_LENGTH = 1024;

    /** Utility class; no instances. */
    private ProtocolConstants() {
    }
}
