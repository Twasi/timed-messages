package at.saith.twasi.service.exception;

public class TooLowIntervalException extends TimerException {

    public TooLowIntervalException(){}
    public TooLowIntervalException(String message) {
        super(message);
    }
}
