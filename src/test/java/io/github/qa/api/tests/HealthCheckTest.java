package io.github.qa.api.tests;

import io.github.qa.api.client.ApiResponse;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Health check")
public class HealthCheckTest extends BaseTest {

    @Test(groups = {"smoke", "regression"}, description = "Service is up and answers /ping")
    @Severity(SeverityLevel.BLOCKER)
    public void serviceIsUp() {
        ApiResponse response = api.get("/ping");

        assertThat(response.status()).isEqualTo(201);
        assertThat(response.durationMs()).as("response time, ms").isLessThan(5000);
    }
}
