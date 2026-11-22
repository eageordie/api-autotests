package io.github.qa.api.client;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import io.github.qa.api.config.Config;
import io.github.qa.api.utils.Json;
import io.qameta.allure.Allure;

import java.util.Map;
import java.util.TreeMap;

/**
 * Thin wrapper over Playwright APIRequestContext.
 * Every request and response is attached to the Allure report,
 * so a failed test carries its own evidence.
 */
public class ApiClient implements AutoCloseable {

    private final Playwright playwright;
    private final APIRequestContext request;

    public ApiClient() {
        this.playwright = Playwright.create();
        this.request = playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(Config.baseUrl())
                .setTimeout(Config.timeoutMs())
                .setExtraHTTPHeaders(Map.of(
                        "Accept", "application/json",
                        "Content-Type", "application/json")));
    }

    public ApiResponse get(String path) {
        return send("GET", path, null, Map.of());
    }

    public ApiResponse get(String path, Map<String, String> query) {
        RequestOptions options = RequestOptions.create();
        query.forEach(options::setQueryParam);
        return execute("GET", path + queryString(query), null, () -> request.get(path, options));
    }

    public ApiResponse post(String path, Object body) {
        return send("POST", path, body, Map.of());
    }

    public ApiResponse put(String path, Object body, Map<String, String> headers) {
        return send("PUT", path, body, headers);
    }

    public ApiResponse patch(String path, Object body, Map<String, String> headers) {
        return send("PATCH", path, body, headers);
    }

    public ApiResponse delete(String path, Map<String, String> headers) {
        return send("DELETE", path, null, headers);
    }

    private ApiResponse send(String method, String path, Object body, Map<String, String> headers) {
        RequestOptions options = RequestOptions.create().setMethod(method);
        headers.forEach(options::setHeader);
        String payload = body == null ? null : (body instanceof String s ? s : Json.write(body));
        if (payload != null) {
            options.setData(payload);
        }
        return execute(method, path, payload, () -> request.fetch(path, options));
    }

    private ApiResponse execute(String method, String path, String payload,
                                java.util.function.Supplier<APIResponse> call) {
        long start = System.currentTimeMillis();
        APIResponse response = call.get();
        long duration = System.currentTimeMillis() - start;

        Map<String, String> headers = new TreeMap<>();
        response.headers().forEach((k, v) -> headers.put(k.toLowerCase(), v));
        ApiResponse result = new ApiResponse(response.status(), response.text(), headers, duration);

        String requestText = method + " " + Config.baseUrl() + path
                + (payload == null ? "" : "\n\n" + payload);
        String responseText = "HTTP " + result.status() + " (" + duration + " ms)\n\n" + Json.pretty(result.body());
        Allure.addAttachment("Request: " + method + " " + path, "text/plain", requestText);
        Allure.addAttachment("Response: " + result.status(), "text/plain", responseText);
        return result;
    }

    private static String queryString(Map<String, String> query) {
        if (query.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("?");
        query.forEach((k, v) -> sb.append(sb.length() > 1 ? "&" : "").append(k).append('=').append(v));
        return sb.toString();
    }

    @Override
    public void close() {
        request.dispose();
        playwright.close();
    }
}
