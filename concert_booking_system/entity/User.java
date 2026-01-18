package concert_booking_system.entity;

import java.util.Map;

public class User {

    private String userId;
    private String contactDetails;
    private Map<String, Object> metadata;

    public User(String userId, String contactDetails, Map<String, Object> metadata) {
        this.userId = userId;
        this.contactDetails = contactDetails;
        this.metadata = metadata;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(String contactDetails) {
        this.contactDetails = contactDetails;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

}
