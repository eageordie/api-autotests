package io.github.qa.api.client;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.qa.api.utils.Json;

import java.util.Map;

/**
 * A detached copy of an HTTP response, so it stays readable
 * after the Playwright request context is disposed.
 */
public record ApiResponse(int status, String body, Map<String, String> headers, long durationMs) {

    public <T> T as(Class<T> type) {
        return Json.read(body, type);
    }

    public <T> T as(TypeReference<T> type) {
        return Json.read(body, type);
    }

    public String header(String name) {
        return headers.get(name.toLowerCase());
    }
}
