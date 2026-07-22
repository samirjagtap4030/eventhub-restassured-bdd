package pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventResponse {
    private EventData data;

    public EventData getData() { return data; }
    public void setData(EventData data) { this.data = data; }
}
