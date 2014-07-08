package org.julp;

public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 9204596480980121316L;

    public DataAccessException(Throwable cause) {
        super(cause);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException() {
        super();
    }
}
