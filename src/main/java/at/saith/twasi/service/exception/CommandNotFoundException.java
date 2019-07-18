package at.saith.twasi.service.exception;

public class CommandNotFoundException extends TimerException {

    public CommandNotFoundException() {
    }

    public CommandNotFoundException(String message) {
        super(message);
    }
}
