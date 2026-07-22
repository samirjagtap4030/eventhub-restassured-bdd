package files;

public class payload {

    public static String addBook(String name, String isbn, String aisle, String author) {
        return "{\r\n"
            + "\"name\":\"" + name + "\",\r\n"
            + "\"isbn\":\"" + isbn + "\",\r\n"
            + "\"aisle\":\"" + aisle + "\",\r\n"
            + "\"author\":\"" + author + "\"\r\n"
            + "}";
    }

    public static String deleteBook(String id) {
        return "{\r\n"
            + "\"ID\":\"" + id + "\"\r\n"
            + "}";
    }
}
