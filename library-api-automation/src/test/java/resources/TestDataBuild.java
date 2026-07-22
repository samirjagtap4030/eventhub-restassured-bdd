package resources;

import files.payload;

public class TestDataBuild {

    // Fixed aisle matches the Postman collection body
    public static final String AISLE = "2529857";

    // ISBN = global companyCode "RS" + time-based suffix (unique per run)
    public String generateIsbn() {
        return "RS" + (System.currentTimeMillis() % 100000);
    }

    public String addBookPayload(String name, String isbn, String author) {
        return payload.addBook(name, isbn, AISLE, author);
    }

    public String deleteBookPayload(String bookId) {
        return payload.deleteBook(bookId);
    }
}
