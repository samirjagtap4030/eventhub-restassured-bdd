package stepDefinations;

import java.io.IOException;

import io.cucumber.java.After;
import io.cucumber.java.Before;

public class Hooks {

    @Before("@Bookings")
    public void beforeBookingsScenario() throws IOException {
        StepDefination m = new StepDefination();
        if (StepDefination.token == null) {
            m.setupLoginAndEvent();
        }
    }

    @Before("@NeedsBooking")
    public void beforeNeedsBookingScenario() throws IOException {
        StepDefination m = new StepDefination();
        if (StepDefination.token == null || StepDefination.eventId == 0) {
            m.setupLoginAndEvent();
        }
        m.setupFreshBooking();
    }

    @After("@NeedsBooking")
    public void cleanupBookingState() {
        StepDefination.bookingId  = 0;
        StepDefination.bookingRef = null;
    }
}
