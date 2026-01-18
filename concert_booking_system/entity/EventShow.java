package concert_booking_system.entity;

public class EventShow {

    private final String eventId;
    private final Long eventDate;
    private final Venue venue;

    public EventShow(String eventId, Long eventDate, Venue venue) {
        this.eventId = eventId;
        this.eventDate = eventDate;
        this.venue = venue;
    }

    public String getEventId() {
        return eventId;
    }

    public Long getEventDate() {
        return eventDate;
    }

    public Venue getVenue() {
        return venue;
    }

}
