package pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingListResponse {
    private List<BookingData> data;
    private Pagination pagination;

    public List<BookingData> getData() { return data; }
    public void setData(List<BookingData> data) { this.data = data; }
    public Pagination getPagination() { return pagination; }
    public void setPagination(Pagination pagination) { this.pagination = pagination; }
}
