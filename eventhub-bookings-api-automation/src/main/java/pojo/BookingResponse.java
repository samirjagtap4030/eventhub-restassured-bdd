package pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingResponse {
    private BookingData data;

    public BookingData getData() { return data; }
    public void setData(BookingData data) { this.data = data; }
}
