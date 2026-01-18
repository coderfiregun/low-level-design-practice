package concert_booking_system;

import concert_booking_system.entity.EventShow;
import concert_booking_system.entity.Seat;
import concert_booking_system.entity.Ticket;
import concert_booking_system.entity.Venue;
import concert_booking_system.enums.SeatStatus;
import concert_booking_system.enums.TicketStatus;
import concert_booking_system.exception.PaymentProcessingException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class BookingSystem {

    private static final BookingSystem INSTANCE = new BookingSystem();

    private final Map<String, EventShow> eventsMap;
    private final Map<String, Ticket> ticketsMap;
    private final Map<String, Map<String, ReentrantLock>> seatLocks;

    private BookingSystem() {
        eventsMap = new ConcurrentHashMap<>();
        ticketsMap = new ConcurrentHashMap<>();
        seatLocks = new ConcurrentHashMap<>();
    }

    public static BookingSystem getInstance() {
        return INSTANCE;
    }

    // Admin operations
    public void addEvent(EventShow eventShow) {
        eventsMap.put(eventShow.getEventId(), eventShow);
    }

    public void removeEvent(EventShow eventShow) {
        eventsMap.remove(eventShow.getEventId());
    }

    public EventShow getEventDetails(String eventId) {
        if (!eventsMap.containsKey(eventId)) {
            throw new IllegalArgumentException("No Such Event Found");
        }
        return eventsMap.get(eventId);
    }

    /**
     * Regular booking: acquires locks, checks availability, processes payment and books.
     */
    public Ticket bookTicket(String userId,
                             String eventId,
                             List<String> seatIds) {
        // Input validation
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be null or empty");
        }
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Seat IDs list cannot be null or empty");
        }

        // Validate event exists
        EventShow event = eventsMap.get(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found: " + eventId);
        }

        // Get actual seat objects from event's venue
        List<Seat> seats = getSeatsFromVenue(event.getVenue(), seatIds);
        if (seats.size() != seatIds.size()) {
            throw new IllegalArgumentException("Some seats not found in venue");
        }

        List<ReentrantLock> allocatedSeatLocks = new ArrayList<>();
        String ticketId = UUID.randomUUID().toString();
        long totalPrice = calculateTicketFare(seats);
        Ticket ticket = new Ticket(ticketId,
                userId,
                eventId,
                seats,
                totalPrice,
                System.currentTimeMillis());

        try {
            allocatedSeatLocks = acquireLockOnSeats(eventId, seats);

            // Check availability once we hold locks
            for (Seat seat : seats) {
                if (seat.getSeatStatus() != SeatStatus.AVAILABLE) {
                    throw new IllegalStateException("Seat " + seat.getSeatId() + " is not available");
                }
            }

            paymentProcess(ticket);
            for (Seat seat : seats) {
                seat.setSeatStatus(SeatStatus.BOOKED);
            }
            ticket.setTicketStatus(TicketStatus.BOOKED);
            ticketsMap.put(ticketId, ticket);
        } catch (Exception ex) {
            if (ex instanceof PaymentProcessingException) {
                refundProcessor(ticket);
            }
            // propagate failure to caller
            throw new RuntimeException("Booking failed: " + ex.getMessage(), ex);
        } finally {
            releaseSeatLocks(allocatedSeatLocks);
        }
        return ticket;
    }

    /**
     * Booking used for testing concurrency: acquires locks and sleeps (holds locks) for holdMillis
     * before proceeding. This simulates a long-running booking while holding seat locks.
     */
    public Ticket bookTicketWithHold(String userId,
                                     String eventId,
                                     List<String> seatIds,
                                     long holdMillis) {
        // Input validation
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be null or empty");
        }
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Seat IDs list cannot be null or empty");
        }

        // Validate event exists
        EventShow event = eventsMap.get(eventId);
        if (event == null) {
            throw new IllegalArgumentException("Event not found: " + eventId);
        }

        // Get actual seat objects from event's venue
        List<Seat> seats = getSeatsFromVenue(event.getVenue(), seatIds);
        if (seats.size() != seatIds.size()) {
            throw new IllegalArgumentException("Some seats not found in venue");
        }

        List<ReentrantLock> allocatedSeatLocks = new ArrayList<>();
        String ticketId = UUID.randomUUID().toString();
        long totalPrice = calculateTicketFare(seats);
        Ticket ticket = new Ticket(ticketId,
                userId,
                eventId,
                seats,
                totalPrice,
                System.currentTimeMillis());

        try {
            allocatedSeatLocks = acquireLockOnSeats(eventId, seats);

            // Informative print: locks have been acquired
            System.out.println("[BookingSystem] User=" + userId + " acquired locks on seats: " + seatIdsList(seats) + " - holding for " + holdMillis + "ms");

            // Hold the locks intentionally to simulate a long operation
            try {
                Thread.sleep(holdMillis);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while holding locks", ie);
            }

            // After holding, check availability
            for (Seat seat : seats) {
                if (seat.getSeatStatus() != SeatStatus.AVAILABLE) {
                    throw new IllegalStateException("Seat " + seat.getSeatId() + " is not available");
                }
            }

            paymentProcess(ticket);
            for (Seat seat : seats) {
                seat.setSeatStatus(SeatStatus.BOOKED);
            }
            ticket.setTicketStatus(TicketStatus.BOOKED);
            ticketsMap.put(ticketId, ticket);
        } catch (Exception ex) {
            if (ex instanceof PaymentProcessingException) {
                refundProcessor(ticket);
            }
            throw new RuntimeException("Booking failed: " + ex.getMessage(), ex);
        } finally {
            releaseSeatLocks(allocatedSeatLocks);
        }
        return ticket;
    }

    private List<ReentrantLock> acquireLockOnSeats(String eventId,
                                                   List<Seat> seats) {
        List<ReentrantLock> locks = new ArrayList<>();
        
        // Sort seats by seatId to ensure consistent lock ordering and prevent deadlocks
        List<Seat> sortedSeats = new ArrayList<>(seats);
        sortedSeats.sort(Comparator.comparing(Seat::getSeatId));
        
        try {
            for (Seat seat : sortedSeats) {
                ReentrantLock lock = seatLocks.computeIfAbsent(eventId,
                                _ -> new ConcurrentHashMap<>())
                        .computeIfAbsent(seat.getSeatId(),
                                _ -> new ReentrantLock());
                locks.add(lock);
                lock.lockInterruptibly();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            // release any locks we acquired before failure
            releaseSeatLocks(locks);
            throw new IllegalArgumentException("Unable to acquire lock: interrupted", ex);
        } catch (Exception ex) {
            // release any locks we acquired before failure
            releaseSeatLocks(locks);
            throw new IllegalArgumentException("Unable to acquire lock", ex);
        }

        return locks;
    }

    private void releaseSeatLocks(List<ReentrantLock> allocatedSeatLocks) {
        if (allocatedSeatLocks == null || allocatedSeatLocks.isEmpty()) {
            return;
        }

        for (ReentrantLock lock : allocatedSeatLocks) {
            if (lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                } catch (IllegalMonitorStateException ignored) {
                }
            }
        }
    }

    private static String seatIdsList(List<Seat> seats) {
        StringBuilder sb = new StringBuilder();
        for (Seat s : seats) {
            if (!sb.isEmpty()) sb.append(",");
            sb.append(s.getSeatId());
        }
        return sb.toString();
    }

    public void cancelTicket(String ticketId) {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket ID cannot be null or empty");
        }
        
        Ticket ticket = ticketsMap.get(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("No Such Ticket Found: " + ticketId);
        }
        
        // Validate ticket can be cancelled
        if (ticket.getTicketStatus() == TicketStatus.CANCELLED) {
            throw new IllegalStateException("Ticket is already cancelled");
        }
        
        if (ticket.getTicketStatus() != TicketStatus.BOOKED) {
            throw new IllegalStateException("Only booked tickets can be cancelled");
        }
        
        List<ReentrantLock> allocatedSeatLocks = new ArrayList<>();
        try {
            // Acquire locks on seats before cancelling
            allocatedSeatLocks = acquireLockOnSeats(ticket.getEventId(), ticket.getBookedSeats());
            
            cancelProcessor(ticket);
            ticket.setTicketStatus(TicketStatus.CANCELLED);
        } catch (Exception ex) {
            throw new RuntimeException("Cancellation failed: " + ex.getMessage(), ex);
        } finally {
            releaseSeatLocks(allocatedSeatLocks);
        }
    }

    private void cancelProcessor(Ticket ticket) {
        // Release seats back to AVAILABLE status
        for (Seat seat : ticket.getBookedSeats()) {
            seat.setSeatStatus(SeatStatus.AVAILABLE);
        }
    }

    public void paymentProcess(Ticket ticket) throws PaymentProcessingException {
        // Simulate payment processing - in real system, this would call payment gateway
        if (ticket == null) {
            throw new PaymentProcessingException("Ticket cannot be null");
        }
        
        // Simulate a small chance of payment failure for testing
        if (Math.random() < 0.05) { // 5% failure rate
            throw new PaymentProcessingException("Payment processing failed");
        }
        
        // Payment successful - no exception thrown
    }

    public long calculateTicketFare(List<Seat> seats) {
        if (seats == null || seats.isEmpty()) {
            return 0;
        }
        
        long totalPrice = 0;
        for (Seat seat : seats) {
            switch (seat.getSeatType()) {
                case REGULAR:
                    totalPrice += 100;
                    break;
                case PREMIUM:
                    totalPrice += 200;
                    break;
                case VIP:
                    totalPrice += 500;
                    break;
                default:
                    totalPrice += 100; // default to regular pricing
            }
        }
        return totalPrice;
    }

    private void refundProcessor(Ticket ticket) {
        // In a real system, this would process refund through payment gateway
        // For now, just log or handle the refund logic
        if (ticket != null) {
            // Refund logic would go here
            // For example: paymentGateway.refund(ticket.getTotalPrice());
        }
    }
    
    private List<Seat> getSeatsFromVenue(Venue venue, List<String> seatIds) {
        if (venue == null || venue.getSeats() == null) {
            throw new IllegalArgumentException("Venue or seats list is null");
        }
        
        List<Seat> requestedSeats = new ArrayList<>();
        Map<String, Seat> seatMap = new HashMap<>();
        
        // Create a map for quick lookup
        for (Seat seat : venue.getSeats()) {
            seatMap.put(seat.getSeatId(), seat);
        }
        
        // Get seats by IDs
        for (String seatId : seatIds) {
            Seat seat = seatMap.get(seatId);
            if (seat == null) {
                throw new IllegalArgumentException("Seat not found in venue: " + seatId);
            }
            requestedSeats.add(seat);
        }
        
        return requestedSeats;
    }

}
