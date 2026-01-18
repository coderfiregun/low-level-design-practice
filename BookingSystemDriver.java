import concert_booking_system.BookingSystem;
import concert_booking_system.entity.*;
import concert_booking_system.enums.SeatStatus;
import concert_booking_system.enums.SeatType;

import java.util.*;
import java.util.concurrent.*;

/**
 * Minimal driver focusing only on the concurrency scenario.
 */
public class BookingSystemDriver {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Booking System Driver ===\n");
        
        BookingSystem bookingSystem = BookingSystem.getInstance();

        // Setup: create venue with different seat types
        Venue venue = createVenueWithMixedSeats("venue-1", "TestLocation", 5);
        EventShow show = new EventShow("event-1", System.currentTimeMillis(), venue);
        bookingSystem.addEvent(show);

        System.out.println("Added event: " + show.getEventId() + "\n");

        // Test 1: Concurrency scenario
        testConcurrency(bookingSystem, show, venue);
        
        // Test 2: Normal booking with different seat types
        testNormalBooking(bookingSystem, show, venue);
        
        // Test 3: Cancellation
        testCancellation(bookingSystem, show, venue);
        
        // Test 4: Error handling
        testErrorHandling(bookingSystem, show, venue);

        System.out.println("\n=== Driver finished ===");
    }

    private static void testConcurrency(BookingSystem bookingSystem, EventShow show, Venue venue) {
        System.out.println("--- Test 1: Concurrency Test ---");
        
        // Target seat IDs (both tasks will attempt these)
        List<String> targetSeatIds = Arrays.asList(venue.getSeats().get(0).getSeatId(), 
                                                    venue.getSeats().get(1).getSeatId());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Task A: acquires locks and holds them (sleep)
        Callable<String> taskA = () -> {
            String userId = "holder-user";
            System.out.println("[Task A] attempting to acquire locks and hold them...");
            try {
                Ticket t = bookingSystem.bookTicketWithHold(userId, show.getEventId(), targetSeatIds, 2000);
                String res = "[Task A] Booking succeeded: " + t.getTicketId() + " status=" + t.getTicketStatus() 
                           + " price=" + t.getTotalPrice();
                System.out.println(res);
                return res;
            } catch (Exception e) {
                String res = "[Task A] Booking failed: " + e.getMessage();
                System.out.println(res);
                return res;
            }
        };

        // Task B: try to book same seats while A is holding locks
        Callable<String> taskB = () -> {
            // wait briefly to ensure A has acquired locks
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            String userId = "racer-user";
            System.out.println("[Task B] attempting to book same seats while A holds locks...");
            try {
                Ticket t = bookingSystem.bookTicket(userId, show.getEventId(), targetSeatIds);
                String res = "[Task B] Booking succeeded: " + t.getTicketId() + " status=" + t.getTicketStatus()
                           + " price=" + t.getTotalPrice();
                System.out.println(res);
                return res;
            } catch (Exception e) {
                String res = "[Task B] Booking failed: " + e.getMessage();
                System.out.println(res);
                return res;
            }
        };

        Future<String> fa = executor.submit(taskA);
        Future<String> fb = executor.submit(taskB);

        // Wait for both to finish
        try {
            System.out.println("[Main] Waiting for tasks to complete...");
            String ra = fa.get();
            String rb = fb.get();
            System.out.println("[Main] Results:\n" + ra + "\n" + rb);
        } catch (Exception e) {
            System.out.println("[Main] Error waiting for tasks: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }

        System.out.println("Seat statuses after concurrency test:");
        printSeatStatuses(venue.getSeats());
        System.out.println();
    }

    private static void testNormalBooking(BookingSystem bookingSystem, EventShow show, Venue venue) {
        System.out.println("--- Test 2: Normal Booking with Different Seat Types ---");
        try {
            // Book a VIP seat to demonstrate pricing
            List<String> vipSeatIds = Arrays.asList(venue.getSeats().get(4).getSeatId()); // Last seat is VIP
            Ticket ticket = bookingSystem.bookTicket("user-1", show.getEventId(), vipSeatIds);
            System.out.println("Booking successful: Ticket ID=" + ticket.getTicketId() 
                             + ", Price=" + ticket.getTotalPrice() 
                             + ", Status=" + ticket.getTicketStatus());
        } catch (Exception e) {
            System.out.println("Booking failed: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testCancellation(BookingSystem bookingSystem, EventShow show, Venue venue) {
        System.out.println("--- Test 3: Ticket Cancellation ---");
        try {
            // First book a seat
            List<String> seatIds = Arrays.asList(venue.getSeats().get(3).getSeatId());
            Ticket ticket = bookingSystem.bookTicket("user-2", show.getEventId(), seatIds);
            System.out.println("Ticket booked: " + ticket.getTicketId() + ", Status: " + ticket.getTicketStatus());
            
            Seat seatBeforeCancel = venue.getSeats().get(3);
            System.out.println("Seat status before cancellation: " + seatBeforeCancel.getSeatStatus());
            
            // Cancel the ticket
            bookingSystem.cancelTicket(ticket.getTicketId());
            System.out.println("Ticket cancelled: " + ticket.getTicketId() + ", Status: " + ticket.getTicketStatus());
            
            Seat seatAfterCancel = venue.getSeats().get(3);
            System.out.println("Seat status after cancellation: " + seatAfterCancel.getSeatStatus());
        } catch (Exception e) {
            System.out.println("Cancellation test failed: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testErrorHandling(BookingSystem bookingSystem, EventShow show, Venue venue) {
        System.out.println("--- Test 4: Error Handling ---");
        
        // Test invalid event ID
        try {
            bookingSystem.bookTicket("user-3", "invalid-event", Arrays.asList("S1"));
        } catch (Exception e) {
            System.out.println("Expected error for invalid event: " + e.getMessage());
        }
        
        // Test invalid seat ID
        try {
            bookingSystem.bookTicket("user-3", show.getEventId(), Arrays.asList("INVALID_SEAT"));
        } catch (Exception e) {
            System.out.println("Expected error for invalid seat: " + e.getMessage());
        }
        
        // Test booking already booked seat
        try {
            List<String> seatIds = Arrays.asList(venue.getSeats().get(0).getSeatId());
            bookingSystem.bookTicket("user-4", show.getEventId(), seatIds);
            bookingSystem.bookTicket("user-5", show.getEventId(), seatIds); // Should fail
        } catch (Exception e) {
            System.out.println("Expected error for already booked seat: " + e.getMessage());
        }
        
        System.out.println();
    }

    private static Venue createVenue(String venueId, String location, int seatCount) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < seatCount; i++) {
            Seat seat = new Seat("S" + (i + 1), SeatType.REGULAR, SeatStatus.AVAILABLE);
            seats.add(seat);
        }
        return new Venue(venueId, location, seats);
    }

    private static Venue createVenueWithMixedSeats(String venueId, String location, int seatCount) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < seatCount; i++) {
            SeatType type = SeatType.REGULAR;
            if (i == seatCount - 1) {
                type = SeatType.VIP; // Last seat is VIP
            } else if (i == seatCount - 2) {
                type = SeatType.PREMIUM; // Second last is Premium
            }
            Seat seat = new Seat("S" + (i + 1), type, SeatStatus.AVAILABLE);
            seats.add(seat);
        }
        return new Venue(venueId, location, seats);
    }

    private static void printSeatStatuses(List<Seat> seats) {
        for (Seat s : seats) {
            System.out.println("Seat " + s.getSeatId() + " -> " + s.getSeatStatus());
        }
    }

}
