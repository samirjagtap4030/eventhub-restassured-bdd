package resources;

public enum APIResources {

    LoginAPI("/api/auth/login"),
    RegisterAPI("/api/auth/register"),
    CreateEventAPI("/api/events"),
    GetEventByIdAPI("/api/events"),        // append /{eventId} in step
    CreateBookingAPI("/api/bookings"),
    GetAllBookingsAPI("/api/bookings"),
    GetBookingByIdAPI("/api/bookings"),     // append /{bookingId} in step
    GetBookingByRefAPI("/api/bookings/ref"),// append /{bookingRef} in step
    CancelBookingAPI("/api/bookings");     // append /{bookingId} in step

    private final String resource;

    APIResources(String resource) { this.resource = resource; }

    public String getResource() { return resource; }
}
