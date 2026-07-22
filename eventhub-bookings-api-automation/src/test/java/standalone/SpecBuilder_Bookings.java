package standalone;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.*;

import java.time.Instant;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import files.ReUsableMethods;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import pojo.BookingData;
import pojo.BookingListResponse;
import pojo.BookingResponse;
import pojo.CreateBookingRequest;
import pojo.CreateEventRequest;
import pojo.EventData;
import pojo.EventResponse;
import pojo.LoginRequest;
import pojo.LoginResponse;

public class SpecBuilder_Bookings {

    private static final String BASE_URL      = "http://localhost:3001";
    private static final String ADMIN_EMAIL    = "xxxxxxxx@gmail.com";
    private static final String ADMIN_PASSWORD = "xxxxxxxxxxx";

    private static RequestSpecification baseSpec;
    private static RequestSpecification authSpec;
    private static int    eventId;
    private static String eventTitle;
    private static double eventPrice;
    private static int    seatsBefore;

    @BeforeClass
    public void setup() {
        baseSpec = new RequestSpecBuilder()
            .setBaseUri(BASE_URL)
            .setContentType(ContentType.JSON)
            .build();

        // Auto-register admin (409 = already exists, fine)
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(ADMIN_EMAIL);
        loginReq.setPassword(ADMIN_PASSWORD);
        given().spec(baseSpec).body(loginReq).when().post("/api/auth/register");

        // Login -- collect token
        Response loginResp = given().spec(baseSpec).body(loginReq)
            .when().post("/api/auth/login")
            .then().statusCode(200)
            .extract().response();
        LoginResponse lr = loginResp.as(LoginResponse.class);
        String token = lr.getToken();
        assertNotNull(token, "Login must return a non-null token");

        // Auth spec used for all protected endpoints
        authSpec = new RequestSpecBuilder()
            .setBaseUri(BASE_URL)
            .setContentType(ContentType.JSON)
            .addHeader("Authorization", "Bearer " + token)
            .build();

        // Create event -- prerequisite for booking tests
        String uniqueTitle = "Test Event " + System.currentTimeMillis();
        CreateEventRequest eventReq = new CreateEventRequest();
        eventReq.setTitle(uniqueTitle);
        eventReq.setCategory("Conference");
        eventReq.setVenue("Tech Park");
        eventReq.setCity("Bangalore");
        eventReq.setEventDate(Instant.now().plusSeconds(7 * 24 * 3600).toString());
        eventReq.setPrice(500);
        eventReq.setTotalSeats(50);

        Response eventResp = given().spec(authSpec).body(eventReq)
            .when().post("/api/events")
            .then().statusCode(201)
            .extract().response();
        EventResponse er = eventResp.as(EventResponse.class);
        EventData ed = er.getData();

        eventId    = ed.getId();
        eventTitle = ed.getTitle();
        eventPrice = Double.parseDouble(ed.getPrice().toString());
        // seatsBefore: use availableSeats if API returns it, otherwise totalSeats (new event)
        seatsBefore = ed.getAvailableSeats() > 0 ? ed.getAvailableSeats() : ed.getTotalSeats();
    }

    // ── Happy Path ────────────────────────────────────────────────────────────

    @Test
    public void happyPath() {
        // TC-102: Create Booking
        CreateBookingRequest bookingReq = new CreateBookingRequest();
        bookingReq.setEventId(eventId);
        bookingReq.setCustomerName("Test User");
        bookingReq.setCustomerEmail("test@example.com");
        bookingReq.setCustomerPhone("9876543210");
        bookingReq.setQuantity(2);

        Response createResp = given().spec(authSpec).body(bookingReq)
            .when().post("/api/bookings")
            .then().statusCode(201)
            .extract().response();
        BookingResponse br = createResp.as(BookingResponse.class);
        BookingData bd = br.getData();

        int    bookingId   = bd.getId();
        String bookingRef  = bd.getBookingRef();
        double totalPrice  = Double.parseDouble(bd.getTotalPrice().toString());

        // TC-102: bookingRef format
        assertTrue(bookingRef.matches("^[A-Z]-[A-Z0-9]{6}$"),
            "bookingRef must match ^[A-Z]-[A-Z0-9]{6}$ but was: " + bookingRef);
        // TC-106: first letter matches event title initial
        assertEquals(bookingRef.charAt(0), Character.toUpperCase(eventTitle.charAt(0)),
            "bookingRef first letter must match event title initial");
        // TC-106: totalPrice = quantity * eventPrice
        assertEquals(totalPrice, 2 * eventPrice, 0.001,
            "totalPrice must equal quantity x eventPrice");
        // TC-102: status = confirmed
        assertEquals(bd.getStatus(), "confirmed");

        // Seat reduction -- Get Event After Booking
        Response eventAfterResp = given().spec(authSpec)
            .when().get("/api/events/" + eventId)
            .then().statusCode(200)
            .extract().response();
        EventData edAfter = eventAfterResp.as(EventResponse.class).getData();
        assertEquals(edAfter.getAvailableSeats(), seatsBefore - 2,
            "availableSeats must decrease by booking quantity");

        // TC-107: Get All Bookings
        Response allResp = given().spec(authSpec)
            .when().get("/api/bookings?page=1&limit=10")
            .then().statusCode(200)
            .extract().response();
        BookingListResponse list = allResp.as(BookingListResponse.class);
        assertNotNull(list.getData(),       "data array must not be null");
        assertNotNull(list.getPagination(), "pagination must not be null");
        assertTrue(list.getPagination().getTotal() >= 1, "total must be >= 1 after booking");

        // Get Booking by ID
        Response byIdResp = given().spec(authSpec)
            .when().get("/api/bookings/" + bookingId)
            .then().statusCode(200)
            .extract().response();
        assertEquals(byIdResp.as(BookingResponse.class).getData().getId(), bookingId);

        // TC-007: Get Booking by Ref
        Response byRefResp = given().spec(authSpec)
            .when().get("/api/bookings/ref/" + bookingRef)
            .then().statusCode(200)
            .extract().response();
        assertEquals(byRefResp.as(BookingResponse.class).getData().getBookingRef(), bookingRef);

        // TC-108: Cancel Booking
        Response cancelResp = given().spec(authSpec)
            .when().delete("/api/bookings/" + bookingId)
            .then().statusCode(200)
            .extract().response();
        // Cancel returns {message: "..."} or {data: "..."} -- flat response, use JsonPath
        String cancelMsg = ReUsableMethods.rawToJson(cancelResp.asString()).getString("message");
        if (cancelMsg == null)
            cancelMsg = ReUsableMethods.rawToJson(cancelResp.asString()).getString("data");
        assertNotNull(cancelMsg, "Cancel response must contain a message");
        assertFalse(cancelMsg.isEmpty(), "Cancel message must not be empty");

        // TC-307: Cancel already-cancelled booking -- expects 404
        given().spec(authSpec)
            .when().delete("/api/bookings/" + bookingId)
            .then().statusCode(404);
    }

    // ── Error Scenarios ───────────────────────────────────────────────────────

    @Test
    public void tc302_insufficientSeats() {
        // quantity=100 exceeds both the max-10 rule and available seats
        CreateBookingRequest req = new CreateBookingRequest();
        req.setEventId(eventId);
        req.setCustomerName("Test User");
        req.setCustomerEmail("test@example.com");
        req.setCustomerPhone("9876543210");
        req.setQuantity(100);

        Response resp = given().spec(authSpec).body(req)
            .when().post("/api/bookings")
            .then().statusCode(400)
            .extract().response();

        String errorMsg = ReUsableMethods.rawToJson(resp.asString()).getString("error");
        if (errorMsg == null)
            errorMsg = ReUsableMethods.rawToJson(resp.asString()).getString("message");
        assertNotNull(errorMsg, "Error response must have an error or message field");

        String body = resp.asString().toLowerCase();
        assertTrue(
            body.contains("seat") || body.contains("insufficient") || body.contains("quantity"),
            "Error must mention seat/insufficient/quantity, got: " + resp.asString()
        );
    }

    @Test
    public void tc304_missingFields() {
        // Only eventId provided -- missing customerName/Email/Phone/quantity
        Response resp = given().spec(authSpec)
            .body("{\"eventId\":" + eventId + "}")
            .when().post("/api/bookings")
            .then().statusCode(400)
            .extract().response();

        String errorMsg = ReUsableMethods.rawToJson(resp.asString()).getString("error");
        if (errorMsg == null)
            errorMsg = ReUsableMethods.rawToJson(resp.asString()).getString("message");
        assertNotNull(errorMsg, "Error response must have an error or message field");
    }

    @Test
    public void tc306_qtyAboveMax() {
        // quantity=11 exceeds the per-booking max of 10
        CreateBookingRequest req = new CreateBookingRequest();
        req.setEventId(eventId);
        req.setCustomerName("Test User");
        req.setCustomerEmail("test@example.com");
        req.setCustomerPhone("9876543210");
        req.setQuantity(11);

        given().spec(authSpec).body(req)
            .when().post("/api/bookings")
            .then().statusCode(400);
    }

    @Test
    public void tc301_nonExistentBooking() {
        // Integer.MAX_VALUE -- guaranteed non-existent
        given().spec(authSpec)
            .when().get("/api/bookings/2147483647")
            .then().statusCode(404);
    }

    // ── Security ──────────────────────────────────────────────────────────────

    @Test
    public void tc203_noAuth() {
        // GET /api/bookings with no Authorization header -- must return 401
        given().spec(baseSpec)
            .when().get("/api/bookings")
            .then().statusCode(401);
    }
}
