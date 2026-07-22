package stepDefinations;

import java.io.IOException;

import io.cucumber.java.Before;

public class Hooks {

    // Runs before every @DeleteBook scenario.
    // If no book_id exists yet (e.g. @DeleteBook is run alone without @AddBook having run first),
    // it adds a book so that book_id is populated and Delete has something to act on.
    @Before("@DeleteBook")
    public void beforeScenario() throws IOException {
        StepDefination m = new StepDefination();
        if (StepDefination.book_id == null) {
            m.add_Book_Payload_with("Postman", "Manisha Agraval");
            m.user_calls_with_http_request("AddBookAPI", "POST");
            m.verify_book_Id_created_maps_to_using("Manisha Agraval", "GetBookAPI");
        }
    }
}
