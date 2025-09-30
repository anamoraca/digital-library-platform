package rs.ac.uns.acs.nais.BookSagaService.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rs.ac.uns.acs.nais.BookSagaService.client.GraphClient;
import rs.ac.uns.acs.nais.BookSagaService.client.TimeseriesClient;
import rs.ac.uns.acs.nais.BookSagaService.dto.BookDto;
import rs.ac.uns.acs.nais.BookSagaService.dto.CreateBookRequest;
import rs.ac.uns.acs.nais.BookSagaService.dto.ErrorLogEvent;
import rs.ac.uns.acs.nais.BookSagaService.model.BookEvent;
import rs.ac.uns.acs.nais.BookSagaService.model.BookEventType;
import rs.ac.uns.acs.nais.BookSagaService.model.BookFormat;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
    public class SagaService {

        private static final String SERVICE_NAME = "book-saga-service";

    private final GraphClient graphClient;
    private final TimeseriesClient timeseriesClient;

    /** Čitanje jedne knjige + audit OPENED.
     *  - Ako Graph padne: loguj error u /api/analytics/errors i propagiraj grešku klijentu.
     *  - Ako audit padne: loguj error, ali NE ruši GET (knjiga ide klijentu).
     */
    public Mono<BookDto> getBook(String id) {
        return graphClient.getById(id)
                .flatMap(book ->
                        timeseriesClient.writeEvent(new BookEvent(
                                        "system",
                                        book.id(),
                                        BookFormat.EPUB,        // po potrebi zamijeni stvarnim formatom
                                        BookEventType.OPENED,
                                        null,                   // loadMs (nemaš ovdje)
                                        null,                   // deltaPages
                                        Instant.now(),
                                        "saga-open-" + book.id() + "-" + UUID.randomUUID()
                                ))
                                .onErrorResume(auditErr ->
                                        timeseriesClient.logError(new ErrorLogEvent(
                                                SERVICE_NAME,
                                                "BOOK_AUDIT_FAILURE",
                                                1,
                                                Instant.now(),
                                                "err-audit-open-" + book.id() + "-" + UUID.randomUUID()
                                        )).onErrorResume(e -> Mono.empty()) // ne želimo kaskade
                                )
                                .thenReturn(book)
                )
                .onErrorResume(graphErr ->
                        timeseriesClient.logError(new ErrorLogEvent(
                                        SERVICE_NAME,
                                        "GRAPH_GET_FAILURE",
                                        1,
                                        Instant.now(),
                                        "err-graph-get-" + id + "-" + UUID.randomUUID()
                                ))
                                .onErrorResume(e -> Mono.empty())
                                .then(Mono.error(graphErr))
                );
    }

    /** CREATE saga (ostavljeno iz prethodnog, ne tražiš izmjenu ovdje, ali je kompletno). */
    public Mono<BookDto> createBook(CreateBookRequest req) {
        return graphClient.create(req)
                .flatMap(created ->
                        timeseriesClient.writeEvent(new BookEvent(
                                        "system",
                                        created.id(),
                                        BookFormat.EPUB,
                                        BookEventType.OPENED,
                                        null,
                                        null,
                                        Instant.now(),
                                        "saga-create-" + created.id() + "-" + UUID.randomUUID()
                                ))
                                .thenReturn(created)
                                .onErrorResume(tsErr ->
                                        graphClient.delete(created.id())
                                                .then(Mono.error(new RuntimeException(
                                                        "Timeseries audit failed, rolled back graph create", tsErr)))
                                )
                );
    }

    /** DELETE saga:
     *  - Ako Graph delete uspije: audit DELETED; ako audit padne → log error (DELETE ostaje uspješan).
     *  - Ako Graph delete padne: log error i propagiraj grešku.
     */
    public Mono<Void> deleteBook(String id) {
        return graphClient.delete(id)
                .then(
                        timeseriesClient.writeEvent(new BookEvent(
                                        "system",
                                        id,
                                        BookFormat.EPUB,
                                        BookEventType.DELETED,  // ← eksplicitno DELETED za brisanje
                                        null, null,
                                        Instant.now(),
                                        "saga-delete-" + id + "-" + UUID.randomUUID()
                                ))
                                .onErrorResume(auditErr ->
                                        timeseriesClient.logError(new ErrorLogEvent(
                                                SERVICE_NAME,
                                                "BOOK_AUDIT_DELETE_FAILURE",
                                                1,
                                                Instant.now(),
                                                "err-audit-delete-" + id + "-" + UUID.randomUUID()
                                        )).onErrorResume(e -> Mono.empty())
                                )
                )
                .onErrorResume(graphErr ->
                        timeseriesClient.logError(new ErrorLogEvent(
                                        SERVICE_NAME,
                                        "GRAPH_DELETE_FAILURE",
                                        1,
                                        Instant.now(),
                                        "err-graph-delete-" + id + "-" + UUID.randomUUID()
                                ))
                                .onErrorResume(e -> Mono.empty())
                                .then(Mono.error(graphErr))
                )
                .then();
    }
}
