package resources;

public enum APIResources {
    AddBookAPI("/Library/Addbook.php"),
    GetBookAPI("/Library/GetBook.php"),
    DeleteBookAPI("/Library/DeleteBook.php");

    private final String resource;

    APIResources(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }
}
