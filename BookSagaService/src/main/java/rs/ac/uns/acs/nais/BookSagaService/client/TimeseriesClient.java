package rs.ac.uns.acs.nais.BookSagaService.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import rs.ac.uns.acs.nais.BookSagaService.model.BookEvent;
import rs.ac.uns.acs.nais.BookSagaService.dto.ErrorLogEvent;

import java.util.List;

@Component
public class TimeseriesClient {

    private static final String BOOKS_BASE  = "/api/analytics/books";
    private static final String ERRORS_BASE = "/api/analytics/errors";

    private final WebClient ts;

    public TimeseriesClient(@Qualifier("tsWebClient") WebClient tsWebClient) {
        this.ts = tsWebClient;
    }

    /* -------------------- BOOK ANALYTICS -------------------- */

    /** Upis jednog BookEvent-a (OPENED/PROGRESS/CLOSED/DELETED). */
    public Mono<Void> writeEvent(BookEvent ev) {
        return ts.post()
                .uri(BOOKS_BASE + "/events")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ev)
                .retrieve()
                .bodyToMono(Void.class);
    }

    /** Batch upis BookEvent-ova (ako zatreba). */
    public Mono<Void> writeEventsBatch(List<BookEvent> events) {
        return ts.post()
                .uri(BOOKS_BASE + "/events/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(events)
                .retrieve()
                .bodyToMono(Void.class);
    }

    /* -------------------- ERROR ANALYTICS -------------------- */

    /** Upis jednog ErrorLogEvent-a. */
    public Mono<Void> logError(ErrorLogEvent e) {
        return ts.post()
                .uri(ERRORS_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(e)
                .retrieve()
                .bodyToMono(Void.class);
    }

    /** Batch upis ErrorLogEvent-ova (opciono). */
    public Mono<Void> logErrorsBatch(List<ErrorLogEvent> events) {
        return ts.post()
                .uri(ERRORS_BASE + "/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(events)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
