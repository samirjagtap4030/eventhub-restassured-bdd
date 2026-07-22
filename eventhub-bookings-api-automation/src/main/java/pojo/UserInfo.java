package pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {
    private int id;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}
