package rs.ac.uns.acs.nais.BookSagaService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RemoteCallException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Mono<Map<String, Object>> handleRemote(RemoteCallException ex) {
        return Mono.just(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Map<String, Object>> handleGeneric(Exception ex) {
        return Mono.just(Map.of("error", "Server error", "details", ex.getMessage()));
    }
}
