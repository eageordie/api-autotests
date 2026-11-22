package io.github.qa.api.config;

/**
 * Test configuration. Every value can be overridden with a system property
 * (-Dbase.url=...) or an environment variable (BASE_URL=...).
 */
public final class Config {

    private Config() {
    }

    public static String baseUrl() {
        return get("base.url", "BASE_URL", "https://restful-booker.herokuapp.com");
    }

    public static String adminUser() {
        return get("admin.user", "ADMIN_USER", "admin");
    }

    public static String adminPassword() {
        return get("admin.password", "ADMIN_PASSWORD", "password123");
    }

    public static double timeoutMs() {
        return Double.parseDouble(get("timeout.ms", "TIMEOUT_MS", "30000"));
    }

    private static String get(String property, String env, String fallback) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(env);
        }
        return value == null || value.isBlank() ? fallback : value;
    }
}
