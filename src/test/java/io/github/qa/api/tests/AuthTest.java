package io.github.qa.api.tests;

import io.github.qa.api.client.ApiResponse;
import io.github.qa.api.config.Config;
import io.github.qa.api.model.AuthResponse;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Authentication")
public class AuthTest extends BaseTest {

    @Test(groups = {"smoke", "regression"}, description = "Valid credentials return a token")
    @Severity(SeverityLevel.BLOCKER)
    public void validCredentialsReturnToken() {
        ApiResponse response = auth.createToken(Config.adminUser(), Config.adminPassword());

        assertThat(response.status()).isEqualTo(200);
        AuthResponse body = response.as(AuthResponse.class);
        assertThat(body.token()).isNotBlank().matches("[a-f0-9]+");
        assertThat(body.reason()).isNull();
    }

    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials() {
        return new Object[][]{
                {"wrong password", Config.adminUser(), "wrong-password"},
                {"unknown user", "no-such-user", Config.adminPassword()},
                {"empty username", "", Config.adminPassword()},
                {"empty password", Config.adminUser(), ""},
                {"SQL injection in username", "admin' OR '1'='1", "x"},
        };
    }

    @Test(groups = "regression", dataProvider = "invalidCredentials",
            description = "Invalid credentials never return a token")
    @Severity(SeverityLevel.CRITICAL)
    public void invalidCredentialsAreRejected(String caseName, String username, String password) {
        ApiResponse response = auth.createToken(username, password);

        // The API answers 200 with a reason instead of 401: documented behavior of this service.
        assertThat(response.status()).as(caseName).isEqualTo(200);
        AuthResponse body = response.as(AuthResponse.class);
        assertThat(body.token()).as(caseName + ": token").isNull();
        assertThat(body.reason()).as(caseName + ": reason").isEqualTo("Bad credentials");
    }
}
