package io.github.qa.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Dates in ISO format: yyyy-MM-dd. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingDates(String checkin, String checkout) {
}
