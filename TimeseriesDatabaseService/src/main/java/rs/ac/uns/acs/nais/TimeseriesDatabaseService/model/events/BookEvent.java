package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events;

import java.time.Instant;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.enums.BookEventType;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.enums.BookFormat;
public record BookEvent(
        String userId,
        String bookId,
        BookFormat format,
        BookEventType event,
        Double loadMs,     // za OPENED
        Integer deltaPages, // za PROGRESS
        Instant ts,
        String idempotencyKey
) {
}