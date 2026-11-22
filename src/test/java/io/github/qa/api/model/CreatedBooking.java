package io.github.qa.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response of POST /booking. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreatedBooking(Integer bookingid, Booking booking) {
}
