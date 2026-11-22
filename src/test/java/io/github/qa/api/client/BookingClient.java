package io.github.qa.api.client;

import io.github.qa.api.model.Booking;
import io.qameta.allure.Step;

import java.util.Map;

public class BookingClient {

    private final ApiClient api;

    public BookingClient(ApiClient api) {
        this.api = api;
    }

    @Step("Create booking")
    public ApiResponse create(Object booking) {
        return api.post("/booking", booking);
    }

    @Step("Get booking {id}")
    public ApiResponse get(int id) {
        return api.get("/booking/" + id);
    }

    @Step("Search bookings by {filter}")
    public ApiResponse search(Map<String, String> filter) {
        return api.get("/booking", filter);
    }

    @Step("Replace booking {id}")
    public ApiResponse update(int id, Booking booking, String token) {
        return api.put("/booking/" + id, booking, auth(token));
    }

    @Step("Partially update booking {id}")
    public ApiResponse patch(int id, Map<String, Object> fields, String token) {
        return api.patch("/booking/" + id, fields, auth(token));
    }

    @Step("Delete booking {id}")
    public ApiResponse delete(int id, String token) {
        return api.delete("/booking/" + id, auth(token));
    }

    private static Map<String, String> auth(String token) {
        return token == null ? Map.of() : Map.of("Cookie", "token=" + token);
    }
}
