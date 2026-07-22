package stepDefinations;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;

import java.io.IOException;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import pojo.BookingListResponse;
import pojo.BookingResponse;
import pojo.EventResponse;
import resources.APIResources;
import resources.TestDataBuild;
import resources.Utils;

public class StepDefination extends Utils {

    // ── Shared static state (set by Hooks and steps) ──────────────────────────
    static String token;
    static int    eventId;
    static String eventTitle;
    static double eventPrice;
    static int    seatsBefore;
    static int    bookingId;
    static String bookingRef;
    static String overridePath;

    // ── Per-scenario instance state ───────────────────────────────────────────
    RequestSpecification res;
    Response             response;
    TestDataBuild        data = new TestDataBuild();

    // ═════════════════════════════════════════════════════════════════════════
    // Scenario 1 — TC-102: Create booking and verify ref, price, seat reduction
    // ═════════════════════════════════════════════════════════════════════════

    @Given("a booking payload with customer {string} {string} {string} and quantity {int}")
    public void booking_payload_with_customer(String name, String email, String phone, int qty)
            throws IOException {
        res = given().spec(requestSpecification())
            .header("Authorization", "Bearer " + token)
            .body(data.createBookingPayload(eventId, name, email, phone, qty));
    }

    // shared step — reused by every scenario that sends an HTTP request
    @When("user calls {string} with {string} http request")
    public void user_calls_with_http_request(String resource, String method) {
        APIResources resourceAPI = APIResources.valueOf(resource);
        String path = resolvePath(resourceAPI);

        if (method.equalsIgnoreCase("POST"))
            response = res.when().post(path);
        else if (method.equalsIgnoreCase("GET"))
            response = res.when().get(path);
        else if (method.equalsIgnoreCase("DELETE"))
            response = res.when().delete(path);
    }

    // shared step — reused by every scenario that checks a status code
    @Then("the API call got success with status code {int}")
    public void status_code(int expected) {
        assertEquals(response.getStatusCode(), expected);
    }

    // shared step — reused by any scenario that checks a JSON field value
    @Then("{string} in response body is {string}")
    public void field_in_response_body_is(String key, String expected) {
        assertEquals(getJsonPath(response, key), expected);
    }

    @Then("booking ref in response matches pattern {string}")
    public void booking_ref_matches_pattern(String pattern) {
        bookingRef = getJsonPath(response, "data.bookingRef");
        bookingId  = Integer.parseInt(getJsonPath(response, "data.id"));
        assertTrue("Expected bookingRef matching " + pattern + " but was: " + bookingRef,
            bookingRef.matches(pattern));
    }

    @Then("total price in response equals {int} times event price")
    public void total_price_equals_qty_times_event_price(int qty) {
        double actual = Double.parseDouble(getJsonPath(response, "data.totalPrice"));
        assertEquals(qty * eventPrice, actual, 0.001);
    }

    @Then("available seats for event decrease by {int} after booking")
    public void seats_decrease_by(int qty) throws IOException {
        Response eventResp = given().spec(requestSpecification())
            .header("Authorization", "Bearer " + token)
            .when().get(APIResources.GetEventByIdAPI.getResource() + "/" + eventId);
        int after = eventResp.as(EventResponse.class).getData().getAvailableSeats();
        assertEquals(seatsBefore - qty, after);
        seatsBefore = after;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Scenario 2 — TC-107: List all bookings returns paginated data
    // ═════════════════════════════════════════════════════════════════════════

    @Given("user requests all bookings with page 1 and limit 10")
    public void user_requests_all_bookings() throws IOException {
        res = given().spec(requestSpecification())
            .header("Authorization", "Bearer " + token)
            .queryParam("page", 1)
            .queryParam("limit", 10);
    }

    // @When user calls  ← shared, already defined above
    // @Then status code ← shared, already defined above

    @Then("response contains a bookings list with pagination")
    public void response_contains_bookings_list_with_pagination() {
        BookingListResponse list = response.as(BookingListResponse.class);
        assertNotNull(list.getData());
        assertNotNull(list.getPagination());
        assertTrue(list.getPagination().getTotal() >= 1);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Scenario 3 — Get booking by ID returns matching booking
    // ═════════════════════════════════════════════════════════════════════════

    @Given("user requests the saved booking by ID")
    public void user_requests_booking_by_id() throws IOException {
        res = given().spec(requestSpecification())
            .header("Authorization", "Bearer " + token);
    }

    // @When user calls          ← shared
    // @Then status code         ← shared
    // @And  field in response   ← shared

    // ═════════════════════════════════════════════════════════════════════════
    // Scenario 4 — TC-007: Get booking by reference returns matching booking
    // ═════════════════════════════════════════════════════════════════════════

    @Given("user requests the saved booking by reference")
    public void user_requests_booking_by_ref() throws IOException {
        res = given().spec(requestSpecification())
            .header("Authorization", "Bearer " + token);
    }

    // @When user calls  ← shared
    // @Then status code ← shared

    // ═════════════════════════════════════════════════════════════════════════
    // Scenario 5 — TC-108 + TC-307: Cancel booking; second cancel returns 404
    // ═════════════════════════════════════════════════════════════════════════

    @Given("user cancels the saved booking")
    public void user_cancels_saved_booking() throws IOException {
        res = given().spec(requestSpecification())
            .header("Authorization", "Bearer " + token);
    }

    // @When user calls  ← shared (DELETE dispatched via resolvePath → CancelBookingAPI)
    // @Then status code ← shared

    // ═════════════════════════════════════════════════════════════════════════
    // Scenario 6 — TC-302 + TC-306: Create booking with invalid quantity → 400
    // ═════════════════════════════════════════════════════════════════════════

    @Given("a booking payload with quantity {int} for the test event")
    public void booking_payload_qty(int qty) throws IOException {
        res = given().spec(requestSpecification())
            .header("Authorization", "Bearer " + token)
            .body(data.createBookingPayloadQty(eventId, qty));
    }

    // @When user calls  ← shared
    // @Then status code ← shared

    @Then("error message is returned")
    public void error_message_is_returned() {
        String body = response.asString();
        assertTrue("Expected error or message field in response: " + body,
            body.contains("error") || body.contains("message"));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Scenario 7 — TC-304: Create booking with missing required fields → 400
    // ═════════════════════════════════════════════════════════════════════════

    @Given("a booking payload with only event ID and no customer details")
    public void booking_payload_missing_fields() throws IOException {
        res = given().spec(requestSpecification())
            .header("Authorization", "Bearer " + token)
            .body(data.missingFieldsPayload(eventId));
    }

    // @When user calls        ← shared
    // @Then status code       ← shared
    // @And  error message     ← shared (defined in Scenario 6)

    // ═════════════════════════════════════════════════════════════════════════
    // Scenario 8 — TC-301: Get non-existent booking → 404
    // ═════════════════════════════════════════════════════════════════════════

    @Given("user requests a booking with non-existent ID {int}")
    public void user_requests_non_existent_booking(int id) throws IOException {
        overridePath = APIResources.GetBookingByIdAPI.getResource() + "/" + id;
        res = given().spec(requestSpecification())
            .header("Authorization", "Bearer " + token);
    }

    // @When user calls  ← shared
    // @Then status code ← shared

    // ═════════════════════════════════════════════════════════════════════════
    // Scenario 9 — TC-203: Get bookings without auth token → 401
    // ═════════════════════════════════════════════════════════════════════════

    @Given("user sends an unauthenticated request to get bookings")
    public void user_sends_unauthenticated_request() throws IOException {
        res = given().spec(requestSpecification()); // no Authorization header
    }

    // @When user calls  ← shared
    // @Then status code ← shared

    // ═════════════════════════════════════════════════════════════════════════
    // Setup helpers — called by Hooks, not by Cucumber steps
    // ═════════════════════════════════════════════════════════════════════════

    void setupLoginAndEvent() throws IOException {
        if (token == null) {
            given().spec(requestSpecification())
                .body(data.loginPayload(
                    getGlobalValue("adminEmail"),
                    getGlobalValue("adminPassword")))
                .when().post(APIResources.RegisterAPI.getResource());

            response = given().spec(requestSpecification())
                .body(data.loginPayload(
                    getGlobalValue("adminEmail"),
                    getGlobalValue("adminPassword")))
                .when().post(APIResources.LoginAPI.getResource());
            token = getJsonPath(response, "token");
        }

        if (eventId == 0) {
            String uniqueTitle = "Test Event " + System.currentTimeMillis();
            response = given().spec(requestSpecification())
                .header("Authorization", "Bearer " + token)
                .body(data.createEventPayload(uniqueTitle))
                .when().post(APIResources.CreateEventAPI.getResource());

            EventResponse er = response.as(EventResponse.class);
            eventId    = er.getData().getId();
            eventTitle = er.getData().getTitle();
            eventPrice = Double.parseDouble(er.getData().getPrice().toString());
            int av = er.getData().getAvailableSeats();
            seatsBefore = av > 0 ? av : er.getData().getTotalSeats();
        }
    }

    void setupFreshBooking() throws IOException {
        response = given().spec(requestSpecification())
            .header("Authorization", "Bearer " + token)
            .body(data.createBookingPayload(
                eventId, "Hook User", "hook@example.com", "9000000000", 1))
            .when().post(APIResources.CreateBookingAPI.getResource());

        BookingResponse br = response.as(BookingResponse.class);
        bookingId  = br.getData().getId();
        bookingRef = br.getData().getBookingRef();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Path resolver — appends runtime IDs to base enum paths where needed
    // ─────────────────────────────────────────────────────────────────────────

    private String resolvePath(APIResources resourceAPI) {
        if (overridePath != null) {
            String p = overridePath;
            overridePath = null;
            return p;
        }
        switch (resourceAPI) {
            case GetEventByIdAPI:    return resourceAPI.getResource() + "/" + eventId;
            case GetBookingByIdAPI:  return resourceAPI.getResource() + "/" + bookingId;
            case GetBookingByRefAPI: return resourceAPI.getResource() + "/" + bookingRef;
            case CancelBookingAPI:   return resourceAPI.getResource() + "/" + bookingId;
            default:                 return resourceAPI.getResource();
        }
    }
}
