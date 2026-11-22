package io.github.qa.api.tests;

import io.github.qa.api.client.ApiResponse;
import io.github.qa.api.model.CreatedBooking;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for defects found while writing this suite. They state the EXPECTED behavior,
 * so they fail until the defect is fixed. Kept out of the regression run (group "known-issue")
 * to keep CI green; run them with: mvn test -Dsuite=known-issues.xml
 */
@Feature("Known issues")
public class KnownIssuesTest extends BaseTest {

    @DataProvider(name = "invalidBodies")
    public Object[][] invalidBodies() {
        return new Object[][]{
                {"empty object", "{}"},
                {"only firstname", "{\"firstname\":\"Ann\"}"},
                {"bookingdates missing", "{\"firstname\":\"Ann\",\"lastname\":\"Lee\",\"totalprice\":100,\"depositpaid\":true}"},
        };
    }

    @Test(groups = "known-issue",
            description = "BUG-4: text in numeric field totalprice is accepted")
    @Description("""
            Steps: POST /booking with totalprice = "cheap".
            Expected: 400, totalprice must be a number.
            Actual: 200, the booking is created and the price is silently lost.""")
    @Issue("BUG-4")
    @Severity(SeverityLevel.CRITICAL)
    public void textPriceIsRejected() {
        String body = """
                {"firstname":"Ann","lastname":"Lee","totalprice":"cheap","depositpaid":true,
                 "bookingdates":{"checkin":"2026-12-01","checkout":"2026-12-05"}}""";

        ApiResponse response = createAndCleanUp(body);

        assertThat(response.status()).isEqualTo(400);
    }

    /** A defect may let invalid data through: remove whatever got created. */
    private ApiResponse createAndCleanUp(String body) {
        ApiResponse response = bookings.create(body);
        if (response.status() == 200) {
            registerForCleanup(response.as(CreatedBooking.class).bookingid());
        }
        return response;
    }

    @Test(groups = "known-issue", dataProvider = "invalidBodies",
            description = "BUG-1: invalid booking body returns 500 instead of 400")
    @Description("""
            Steps: POST /booking with an invalid body.
            Expected: 400 Bad Request with a validation message.
            Actual: 500 Internal Server Error, plain-text body.
            Impact: clients cannot tell a validation error from a server failure; 5xx alerts fire on user input.""")
    @Issue("BUG-1")
    @Severity(SeverityLevel.NORMAL)
    public void invalidBodyReturns400(String caseName, String body) {
        ApiResponse response = bookings.create(body);

        assertThat(response.status()).as(caseName).isEqualTo(400);
    }

    @Test(groups = "known-issue",
            description = "BUG-2: checkout before checkin is accepted")
    @Description("""
            Steps: POST /booking with checkout earlier than checkin.
            Expected: 400, the date range is rejected.
            Actual: 200, the booking is created with a negative stay.""")
    @Issue("BUG-2")
    @Severity(SeverityLevel.CRITICAL)
    public void checkoutBeforeCheckinIsRejected() {
        String body = """
                {"firstname":"Ann","lastname":"Lee","totalprice":100,"depositpaid":true,
                 "bookingdates":{"checkin":"2026-12-10","checkout":"2026-12-01"}}""";

        ApiResponse response = createAndCleanUp(body);

        assertThat(response.status()).isEqualTo(400);
    }

    @Test(groups = "known-issue",
            description = "BUG-3: negative total price is accepted")
    @Description("""
            Steps: POST /booking with totalprice = -100.
            Expected: 400, price must be positive.
            Actual: 200, the booking is stored with a negative price.""")
    @Issue("BUG-3")
    @Severity(SeverityLevel.CRITICAL)
    public void negativePriceIsRejected() {
        String body = """
                {"firstname":"Ann","lastname":"Lee","totalprice":-100,"depositpaid":true,
                 "bookingdates":{"checkin":"2026-12-01","checkout":"2026-12-05"}}""";

        ApiResponse response = createAndCleanUp(body);

        assertThat(response.status()).isEqualTo(400);
    }
}
