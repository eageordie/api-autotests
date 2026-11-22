package io.github.qa.api.client;

import io.github.qa.api.config.Config;
import io.github.qa.api.model.AuthResponse;
import io.qameta.allure.Step;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthClient {

    private final ApiClient api;

    public AuthClient(ApiClient api) {
        this.api = api;
    }

    @Step("Request auth token for user {username}")
    public ApiResponse createToken(String username, String password) {
        return api.post("/auth", Map.of("username", username, "password", password));
    }

    /** Valid admin token, ready to be sent as a Cookie header. */
    public String adminToken() {
        ApiResponse response = createToken(Config.adminUser(), Config.adminPassword());
        assertThat(response.status()).as("POST /auth status").isEqualTo(200);
        String token = response.as(AuthResponse.class).token();
        assertThat(token).as("auth token").isNotBlank();
        return token;
    }
}
