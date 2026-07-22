package standalone;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import files.ReUsableMethods;
import files.payload;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

// Section 2 + 8: flat JSON body via dynamic string helper (payload.java) + RequestSpecBuilder.
// No POJO needed — AddBook/DeleteBook payloads are shallow; deserialization skipped for the
// same reason (flat response fields, no nested objects to justify a POJO).
// JUnit4 @Test used (not TestNG) so mvn test -Dtest=SpecBuilder_Library works with the
// surefire-junit47 provider that this project requires for the Cucumber runner.
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpecBuilder_Library {

    private static final String BASE_URL = "http://216.10.245.166";
    // ISBN = company-code prefix (RS, from globals) + time-based suffix for uniqueness
    private static final String ISBN = "RS" + (System.currentTimeMillis() % 100000);
    private static final String AISLE = "2529857";
    private static final String EXPECTED_BOOK_ID = ISBN + AISLE;

    private static String book_id;

    private RequestSpecification req() {
        return new RequestSpecBuilder()
            .setBaseUri(BASE_URL)
            .setContentType(ContentType.JSON)
            .build();
    }

    @Test
    public void t1_addBook_success() {
        Response response = given().spec(req())
            .body(payload.addBook("Postman", ISBN, AISLE, "Manisha Agraval"))
            .when().post("/Library/Addbook.php")
            .then().statusCode(200)
            .extract().response();

        JsonPath js = ReUsableMethods.rawToJson(response.asString());
        String msg = js.getString("Msg");
        book_id = js.getString("ID");

        System.out.println("Msg   : " + msg);
        System.out.println("book_id: " + book_id);

        assertEquals("successfully added", msg);
        // ID must equal isbn + aisle (business rule from the Postman collection)
        assertEquals(EXPECTED_BOOK_ID, book_id);
    }

    @Test
    public void t2_getBook_verifyAuthor() {
        // Depends on t1 having set book_id; @FixMethodOrder guarantees execution order
        if (book_id == null) return;

        Response response = given().spec(req())
            .queryParam("ID", book_id)
            .when().get("/Library/GetBook.php")
            .then().statusCode(200)
            .extract().response();

        // GetBook returns a JSON array; index [0] holds the book object
        String actualAuthor = response.jsonPath().getString("[0].author");
        System.out.println("author: " + actualAuthor);
        assertEquals("Manisha Agraval", actualAuthor);
    }

    @Test
    public void t3_deleteBook_success() {
        if (book_id == null) return;

        Response response = given().spec(req())
            .body(payload.deleteBook(book_id))
            .when().post("/Library/DeleteBook.php")
            .then().statusCode(200)
            .extract().response();

        String msg = ReUsableMethods.rawToJson(response.asString()).getString("msg");
        System.out.println("Delete msg: " + msg);
        assertEquals("book is successfully deleted", msg);
    }
}
