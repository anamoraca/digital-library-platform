package rs.ac.uns.acs.nais.BookSagaService.model;
import java.time.Instant;
import rs.ac.uns.acs.nais.BookSagaService.model.*;

public record BookEvent(
        String userId,
        String bookId,
        BookFormat format,
        BookEventType event,
        Double loadMs,
        Integer deltaPages,
        Instant ts,
        String idempotencyKey

        //TODO: zadnja dva sta su
) {}
