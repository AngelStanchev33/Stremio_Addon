package org.example.stremioaddon.utility;

import java.util.Arrays;
import java.util.List;

/**
 * Common HTTP constants used across subtitle providers
 */
public final class HttpConstants {

    private HttpConstants() {
        // Utility class - prevent instantiation
    }

    public static final List<String> USER_AGENTS = Arrays.asList(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Edge/120.0.0.0 Safari/537.36"
    );

    // Common HTTP headers for realistic browser emulation
    public static final String HEADER_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
    public static final String HEADER_ACCEPT_LANGUAGE = "bg-BG,bg;q=0.9,en-US;q=0.8,en;q=0.7";
    public static final String HEADER_ACCEPT_ENCODING = "gzip, deflate, br";
    public static final String HEADER_DNT = "1";
    public static final String HEADER_UPGRADE_INSECURE = "1";
    public static final String HEADER_CACHE_CONTROL = "max-age=0";

    // Timeout constants (in milliseconds)
    public static final int TIMEOUT_STANDARD = 15000;  // 15 seconds
    public static final int TIMEOUT_DOWNLOAD = 30000;  // 30 seconds

    // Cache constants
    public static final int MAX_ARCHIVE_SIZE_MB = 10;
    public static final int MAX_ARCHIVE_SIZE_BYTES = MAX_ARCHIVE_SIZE_MB * 1024 * 1024;

    // Delay constants (in milliseconds)
    public static final int MIN_DELAY = 0;
    public static final int MAX_DELAY = 1000;

    // Subtitle table parsing
    public static final int YAVKA_MAX_ROWS_TO_PROCESS = 50;
}
