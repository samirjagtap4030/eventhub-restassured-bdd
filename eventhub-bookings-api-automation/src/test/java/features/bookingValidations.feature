Feature: EventHub Bookings API Validation

  # ── Happy Path ──────────────────────────────────────────────────────────────

  @Bookings @HappyPath @Regression
  Scenario Outline: TC-102 Create booking and verify ref format, price and seat reduction
    Given a booking payload with customer "<customerName>" "<email>" "<phone>" and quantity <qty>
    When user calls "CreateBookingAPI" with "POST" http request
    Then the API call got success with status code 201
    And "data.status" in response body is "confirmed"
    And booking ref in response matches pattern "^[A-Z]-[A-Z0-9]{6}$"
    And total price in response equals <qty> times event price
    And available seats for event decrease by <qty> after booking

    Examples:
      | customerName | email            | phone      | qty |
      | Test User    | test@example.com | 9876543210 | 2   |

  @Bookings @NeedsBooking @HappyPath @Regression
  Scenario: TC-107 List all bookings returns paginated data
    Given user requests all bookings with page 1 and limit 10
    When user calls "GetAllBookingsAPI" with "GET" http request
    Then the API call got success with status code 200
    And response contains a bookings list with pagination

  @Bookings @NeedsBooking @HappyPath @Regression
  Scenario: Get booking by ID returns matching booking
    Given user requests the saved booking by ID
    When user calls "GetBookingByIdAPI" with "GET" http request
    Then the API call got success with status code 200
    And "data.status" in response body is "confirmed"

  @Bookings @NeedsBooking @HappyPath @Regression
  Scenario: TC-007 Get booking by reference returns matching booking
    Given user requests the saved booking by reference
    When user calls "GetBookingByRefAPI" with "GET" http request
    Then the API call got success with status code 200

  @Bookings @NeedsBooking @CancelBooking @Regression
  Scenario: TC-108 Cancel booking succeeds; TC-307 second cancel returns 404
    Given user cancels the saved booking
    When user calls "CancelBookingAPI" with "DELETE" http request
    Then the API call got success with status code 200
    When user calls "CancelBookingAPI" with "DELETE" http request
    Then the API call got success with status code 404

  # ── Error Scenarios ───────────────────────────────────────────────────────

  @Bookings @Negative @Regression
  Scenario Outline: TC-302 TC-306 Create booking with invalid quantity returns 400
    Given a booking payload with quantity <qty> for the test event
    When user calls "CreateBookingAPI" with "POST" http request
    Then the API call got success with status code 400
    And error message is returned

    Examples:
      | qty | scenario              |
      | 100 | TC-302 over available |
      | 11  | TC-306 over max 10    |

  @Bookings @Negative @Regression
  Scenario: TC-304 Create booking with missing required fields returns 400
    Given a booking payload with only event ID and no customer details
    When user calls "CreateBookingAPI" with "POST" http request
    Then the API call got success with status code 400
    And error message is returned

  @Bookings @Negative @Regression
  Scenario: TC-301 Get non-existent booking returns 404
    Given user requests a booking with non-existent ID 2147483647
    When user calls "GetBookingByIdAPI" with "GET" http request
    Then the API call got success with status code 404

  # ── Security ──────────────────────────────────────────────────────────────

  @Security @Regression
  Scenario: TC-203 Get bookings without auth token returns 401
    Given user sends an unauthenticated request to get bookings
    When user calls "GetAllBookingsAPI" with "GET" http request
    Then the API call got success with status code 401
