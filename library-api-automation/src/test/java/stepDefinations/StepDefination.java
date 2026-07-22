package stepDefinations;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import resources.APIResources;
import resources.TestDataBuild;
import resources.Utils;

public class StepDefination extends Utils {

    RequestSpecification res;
    Response response;
    TestDataBuild data = new TestDataBuild();

    // Shared across scenarios: isbn for the book_id business-rule check; book_id for Delete
    static String isbn;
    static String book_id;

    @Given("Add Book Payload with {string} {string}")
    public void add_Book_Payload_with(String name, String author) throws IOException {
        isbn = data.generateIsbn();
        res = given().spec(requestSpecification())
            .body(data.addBookPayload(name, isbn, author));
    }

    @When("user calls {string} with {string} http request")
    public void user_calls_with_http_request(String resource, String method) {
        APIResources resourceAPI = APIResources.valueOf(resource);
        if (method.equalsIgnoreCase("POST"))
            response = res.when().post(resourceAPI.getResource());
        else if (method.equalsIgnoreCase("GET"))
            response = res.when().get(resourceAPI.getResource());
    }

    @Then("the API call got success with status code {int}")
    public void the_API_call_got_success_with_status_code(Integer int1) {
        assertEquals(response.getStatusCode(), int1.intValue());
    }

    @Then("{string} in response body is {string}")
    public void in_response_body_is(String keyValue, String expectedValue) {
        assertEquals(getJsonPath(response, keyValue), expectedValue);
    }

    // Extracts book_id, validates the ID business rule (isbn + aisle), then calls GetBook
    // and verifies the stored author matches the Examples-table value.
    @Then("verify book_Id created maps to {string} using {string}")
    public void verify_book_Id_created_maps_to_using(String expectedAuthor, String resource) throws IOException {
        book_id = getJsonPath(response, "ID");

        // Business rule: ID returned by AddBook must equal isbn concatenated with aisle
        assertEquals(book_id, isbn + TestDataBuild.AISLE);

        res = given().spec(requestSpecification()).queryParam("ID", book_id);
        user_calls_with_http_request(resource, "GET");

        // GetBook returns a JSON array — read the first element's author field
        String actualAuthor = response.jsonPath().getString("[0].author");
        assertEquals(actualAuthor, expectedAuthor);
    }

    @Given("Delete Book Payload")
    public void delete_Book_Payload() throws IOException {
        res = given().spec(requestSpecification())
            .body(data.deleteBookPayload(book_id));
    }
}
