package io.github.qa.api.tests;

import io.github.qa.api.client.ApiResponse;
import io.github.qa.api.data.BookingFactory;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Booking: negative and access control")
public class BookingNegativeTest extends BaseTest {

    @DataProvider(name = "badTokens")
    public Object[][] badTokens() {
        return new Object[][]{
                {"no token", null},
                {"random token", "abc123def456"},
                {"empty token", ""},
        };
    }

    @Test(groups = "regression", dataProvider = "badTokens",
            description = "Update without a valid token is forbidden and data stays unchanged")
    @Severity(SeverityLevel.CRITICAL)
    public void updateRequiresValidToken(String caseName, String token) {
        var original = BookingFactory.valid();
        int id = givenBooking(original).bookingid();

        ApiResponse put = bookings.update(id, BookingFactory.valid(), token);

        assertThat(put.status()).as(caseName).isEqualTo(403);
        assertThat(bookings.get(id).body()).as("booking must not change")
                .contains(original.lastname());
    }

    @Test(groups = "regression", dataProvider = "badTokens",
            description = "Delete without a valid token is forbidden and booking still exists")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteRequiresValidToken(String caseName, String token) {
        int id = givenBooking(BookingFactory.valid()).bookingid();

        ApiResponse delete = bookings.delete(id, token);

        assertThat(delete.status()).as(caseName).isEqualTo(403);
        assertThat(bookings.get(id).status()).as("booking still exists").isEqualTo(200);
    }

    @Test(groups = "regression", dataProvider = "badTokens",
            description = "Patch without a valid token is forbidden")
    @Severity(SeverityLevel.CRITICAL)
    public void patchRequiresValidToken(String caseName, String token) {
        int id = givenBooking(BookingFactory.valid()).bookingid();

        ApiResponse patch = bookings.patch(id, Map.of("firstname", "Hacker"), token);

        assertThat(patch.status()).as(caseName).isEqualTo(403);
        assertThat(bookings.get(id).body()).doesNotContain("Hacker");
    }

    @Test(groups = "regression", description = "Malformed JSON body returns 400")
    @Severity(SeverityLevel.NORMAL)
    public void malformedJsonReturns400() {
        ApiResponse response = bookings.create("{\"firstname\":");

        assertThat(response.status()).isEqualTo(400);
    }

    @DataProvider(name = "missingIds")
    public Object[][] missingIds() {
        return new Object[][]{{0}, {-1}, {999_999_999}};
    }

    @Test(groups = "regression", dataProvider = "missingIds",
            description = "Unknown booking id returns 404")
    @Severity(SeverityLevel.NORMAL)
    public void unknownIdReturns404(int id) {
        assertThat(bookings.get(id).status()).isEqualTo(404);
    }
}
