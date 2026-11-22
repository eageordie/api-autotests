package io.github.qa.api.tests;

import io.github.qa.api.client.ApiResponse;
import io.github.qa.api.data.BookingFactory;
import io.github.qa.api.model.Booking;
import io.github.qa.api.model.CreatedBooking;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end chain over one booking: create -> read -> replace -> patch -> delete -> read.
 * Each step uses the id and token from the previous ones, like a Postman collection with environment variables.
 */
@Feature("Booking lifecycle")
public class BookingLifecycleTest extends BaseTest {

    @Test(groups = {"smoke", "regression"}, description = "Create booking: response echoes the request and returns an id")
    @Severity(SeverityLevel.BLOCKER)
    public void createBooking() {
        Booking request = BookingFactory.valid();

        ApiResponse response = bookings.create(request);

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.header("content-type")).contains("application/json");
        CreatedBooking created = response.as(CreatedBooking.class);
        registerForCleanup(created.bookingid());
        assertThat(created.bookingid()).isPositive();
        assertThat(created.booking()).usingRecursiveComparison().isEqualTo(request);
    }

    @Test(groups = "regression", description = "Full booking lifecycle with an auth token")
    @Description("Chained scenario: every step depends on data from the previous step.")
    @Severity(SeverityLevel.CRITICAL)
    public void fullLifecycle() {
        String token = auth.adminToken();
        Booking original = BookingFactory.valid();

        // 1. create
        int id = givenBooking(original).bookingid();

        // 2. read: stored data equals what was sent
        ApiResponse read = bookings.get(id);
        assertThat(read.status()).isEqualTo(200);
        assertThat(read.as(Booking.class)).usingRecursiveComparison().isEqualTo(original);

        // 3. replace the whole booking
        Booking replaced = BookingFactory.valid();
        ApiResponse put = bookings.update(id, replaced, token);
        assertThat(put.status()).isEqualTo(200);
        assertThat(put.as(Booking.class)).usingRecursiveComparison().isEqualTo(replaced);

        // 4. patch two fields; the rest must stay untouched
        ApiResponse patch = bookings.patch(id, Map.of("firstname", "Patched", "totalprice", 999), token);
        assertThat(patch.status()).isEqualTo(200);
        Booking expected = replaced.withFirstname("Patched").withTotalprice(999);
        assertThat(bookings.get(id).as(Booking.class)).usingRecursiveComparison().isEqualTo(expected);

        // 5. delete (the API answers 201 Created on delete)
        ApiResponse delete = bookings.delete(id, token);
        assertThat(delete.status()).isEqualTo(201);
        forget(id);

        // 6. booking is gone
        assertThat(bookings.get(id).status()).isEqualTo(404);
    }

    @Test(groups = "regression", description = "Search by first and last name finds the created booking")
    @Severity(SeverityLevel.NORMAL)
    public void searchByName() {
        Booking booking = BookingFactory.valid();
        int id = givenBooking(booking).bookingid();

        ApiResponse response = bookings.search(Map.of(
                "firstname", booking.firstname(),
                "lastname", booking.lastname()));

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body()).contains("\"bookingid\":" + id);
    }
}
