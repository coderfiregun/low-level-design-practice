package concert_booking_system.exception;

public class PaymentProcessingException extends RuntimeException{
    public PaymentProcessingException(String message) {
        super(message);
    }
}
