package resources;

import pojo.CreateBookingRequest;
import pojo.CreateEventRequest;
import pojo.LoginRequest;

import java.time.Instant;

public class TestDataBuild {

    public LoginRequest loginPayload(String email, String password) {
        LoginRequest lr = new LoginRequest();
        lr.setEmail(email);
        lr.setPassword(password);
        return lr;
    }

    public CreateEventRequest createEventPayload(String title) {
        CreateEventRequest er = new CreateEventRequest();
        er.setTitle(title);
        er.setCategory("Conference");
        er.setVenue("Tech Park");
        er.setCity("Bangalore");
        er.setEventDate(Instant.now().plusSeconds(7 * 24 * 3600).toString());
        er.setPrice(500);
        er.setTotalSeats(100);
        return er;
    }

    public CreateBookingRequest createBookingPayload(int eventId, String customerName,
                                                     String customerEmail, String customerPhone,
                                                     int quantity) {
        CreateBookingRequest br = new CreateBookingRequest();
        br.setEventId(eventId);
        br.setCustomerName(customerName);
        br.setCustomerEmail(customerEmail);
        br.setCustomerPhone(customerPhone);
        br.setQuantity(quantity);
        return br;
    }

    // For quantity-error scenarios (TC-302, TC-306) -- customer fields are valid, only qty varies
    public CreateBookingRequest createBookingPayloadQty(int eventId, int quantity) {
        return createBookingPayload(eventId, "Test User", "test@example.com", "9876543210", quantity);
    }

    // TC-304: only eventId, missing all other required fields
    public String missingFieldsPayload(int eventId) {
        return "{\"eventId\":" + eventId + "}";
    }
}
