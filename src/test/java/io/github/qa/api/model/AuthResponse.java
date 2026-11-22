package io.github.qa.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response of POST /auth: either a token or a reason for failure. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthResponse(String token, String reason) {
}
