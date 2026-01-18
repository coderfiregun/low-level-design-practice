package concert_booking_system.entity;

import concert_booking_system.enums.TicketStatus;

import java.util.List;

import static concert_booking_system.enums.TicketStatus.PENDING;


public class Ticket {

    private final String ticketId;
    private final String userId;
    private final String eventId;
    private final List<Seat> bookedSeats;
    private final long totalPrice;
    private final long bookingTimestamp;
    private TicketStatus ticketStatus;

    public Ticket(String ticketId,
                  String userId,
                  String eventId,
                  List<Seat> bookedSeats,
                  long totalPrice,
                  long bookingTimestamp) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.eventId = eventId;
        this.bookedSeats = bookedSeats;
        this.totalPrice = totalPrice;
        this.bookingTimestamp = bookingTimestamp;
        this.ticketStatus = PENDING;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getUserId() {
        return userId;
    }

    public String getEventId() {
        return eventId;
    }

    public List<Seat> getBookedSeats() {
        return bookedSeats;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public long getBookingTimestamp() {
        return bookingTimestamp;
    }

    public TicketStatus getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(TicketStatus ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

}
