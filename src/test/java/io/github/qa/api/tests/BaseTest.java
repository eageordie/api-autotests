package io.github.qa.api.tests;

import io.github.qa.api.client.ApiClient;
import io.github.qa.api.client.ApiResponse;
import io.github.qa.api.client.AuthClient;
import io.github.qa.api.client.BookingClient;
import io.github.qa.api.model.Booking;
import io.github.qa.api.model.CreatedBooking;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseTest {

    protected ApiClient api;
    protected AuthClient auth;
    protected BookingClient bookings;

    /** Bookings created by tests in this class; removed after the class finishes. */
    private final List<Integer> created = new ArrayList<>();

    @BeforeClass(alwaysRun = true)
    public void setUpClient() {
        api = new ApiClient();
        auth = new AuthClient(api);
        bookings = new BookingClient(api);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        if (api == null) {
            return;
        }
        if (!created.isEmpty()) {
            String token = auth.adminToken();
            created.forEach(id -> bookings.delete(id, token));
        }
        api.close();
    }

    /** Precondition helper: creates a booking and registers it for cleanup. */
    protected CreatedBooking givenBooking(Booking booking) {
        ApiResponse response = bookings.create(booking);
        assertThat(response.status()).as("precondition: POST /booking status").isEqualTo(200);
        CreatedBooking result = response.as(CreatedBooking.class);
        registerForCleanup(result.bookingid());
        return result;
    }

    protected void registerForCleanup(int bookingId) {
        created.add(bookingId);
    }

    protected void forget(int bookingId) {
        created.remove(Integer.valueOf(bookingId));
    }
}
