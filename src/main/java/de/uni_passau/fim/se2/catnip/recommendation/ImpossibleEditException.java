package de.uni_passau.fim.se2.catnip.recommendation;

public class ImpossibleEditException extends Exception {

    public ImpossibleEditException() {
    }

    public ImpossibleEditException(String message) {
        super(message);
    }

    public ImpossibleEditException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImpossibleEditException(Throwable cause) {
        super(cause);
    }

    public ImpossibleEditException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

