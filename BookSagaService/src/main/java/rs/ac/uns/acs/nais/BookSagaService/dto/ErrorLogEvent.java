package rs.ac.uns.acs.nais.BookSagaService.dto;

import java.time.Instant;

public record ErrorLogEvent(
        String service,          // npr. "book-saga-service"
        String errorType,        // npr. "GRAPH_GET_FAILURE" / "BOOK_AUDIT_FAILURE" / "GRAPH_DELETE_FAILURE"
        Integer count,           // obično 1
        Instant ts,              // vrijeme nastanka greške
        String idempotencyKey    // opcioni ključ za deduplikaciju
) { }