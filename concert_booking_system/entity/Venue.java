package concert_booking_system.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Venue {

    private final String venueId;
    private final String location;
    private final List<Seat> seats;

    public Venue(String venueId, String location, List<Seat> seats) {
        this.venueId = venueId;
        this.location = location;
        this.seats = Optional.ofNullable(seats)
                .orElse(new ArrayList<>());
    }

    public String getVenueId() {
        return venueId;
    }

    public String getLocation() {
        return location;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void addSeat(Seat seat) {
        seats.add(seat);
    }

    public void removeSeat(Seat seat) {
        seats.remove(seat);
    }

}
