package rs.ac.uns.acs.nais.BookSagaService.exception;


public class RemoteCallException extends RuntimeException {
    public RemoteCallException(String msg) { super(msg); }
    public RemoteCallException(String msg, Throwable cause) { super(msg, cause); }
}

