package io.github.qa.api.data;

import io.github.qa.api.model.Booking;
import io.github.qa.api.model.BookingDates;
import net.datafaker.Faker;

import java.time.LocalDate;

/** Unique, valid test data for every test, so parallel runs do not collide. */
public final class BookingFactory {

    private static final Faker FAKER = new Faker();

    private BookingFactory() {
    }

    public static Booking valid() {
        LocalDate checkin = LocalDate.now().plusDays(FAKER.number().numberBetween(1, 60));
        LocalDate checkout = checkin.plusDays(FAKER.number().numberBetween(1, 14));
        return new Booking(
                FAKER.name().firstName(),
                FAKER.name().lastName() + "-" + FAKER.number().digits(5),
                FAKER.number().numberBetween(50, 5000),
                FAKER.bool().bool(),
                new BookingDates(checkin.toString(), checkout.toString()),
                FAKER.options().option("Breakfast", "Late checkout", "Airport transfer", "None"));
    }
}
