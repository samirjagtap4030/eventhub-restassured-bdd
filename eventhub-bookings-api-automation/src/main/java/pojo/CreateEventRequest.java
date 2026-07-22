package pojo;

public class CreateEventRequest {
    private String title;
    private String category;
    private String venue;
    private String city;
    private String eventDate;
    private int price;
    private int totalSeats;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
}
