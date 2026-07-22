Feature: Validating Library Book API's

  @AddBook @Regression
  Scenario Outline: Verify if Book is successfully added using AddBookAPI
    Given Add Book Payload with "<name>" "<author>"
    When user calls "AddBookAPI" with "POST" http request
    Then the API call got success with status code 200
    And "Msg" in response body is "successfully added"
    And verify book_Id created maps to "<author>" using "GetBookAPI"

    Examples:
      | name    | author           |
      | Postman | Manisha Agraval  |
      | Git     | Rahul shetty     |
      | Angular | Kajal deshpande  |

  @DeleteBook @Regression
  Scenario: Verify if Delete Book functionality is working
    Given Delete Book Payload
    When user calls "DeleteBookAPI" with "POST" http request
    Then the API call got success with status code 200
    And "msg" in response body is "book is successfully deleted"
