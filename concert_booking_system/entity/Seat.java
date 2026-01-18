package concert_booking_system.entity;

import concert_booking_system.enums.SeatStatus;
import concert_booking_system.enums.SeatType;
import java.util.Objects;

public class Seat {

    private String seatId;
    private SeatType seatType;
    private SeatStatus seatStatus;

    public Seat(String seatId, SeatType seatType, SeatStatus seatStatus) {
        this.seatId = seatId;
        this.seatType = seatType;
        this.seatStatus = seatStatus;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }

    public SeatStatus getSeatStatus() {
        return seatStatus;
    }

    public void setSeatStatus(SeatStatus seatStatus) {
        this.seatStatus = seatStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        return Objects.equals(seatId, seat.seatId)
                && seatType == seat.seatType
                && seatStatus == seat.seatStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seatId, seatType, seatStatus);
    }

}
