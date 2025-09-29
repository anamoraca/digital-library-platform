package rs.ac.uns.acs.nais.BookSagaService.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rs.ac.uns.acs.nais.BookSagaService.client.GraphClient;
import rs.ac.uns.acs.nais.BookSagaService.client.TimeseriesClient;
import rs.ac.uns.acs.nais.BookSagaService.dto.BookDto;
import rs.ac.uns.acs.nais.BookSagaService.dto.CreateBookRequest;
import rs.ac.uns.acs.nais.BookSagaService.dto.TsEventDto;
import rs.ac.uns.acs.nais.BookSagaService.exception.RemoteCallException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SagaService {

   private final GraphClient graph;
    private final TimeseriesClient ts;

    // 1) GET (samo prosleđivanje ka Graph servisu)
    public Mono<BookDto> getBook(String id) {
        return graph.getById(id);
    }

    // 2) CREATE (SAGA: kreiraj u Graph -> loguj u TS; ako TS padne, obriši u Graph – kompenzacija)
    public Mono<BookDto> createBook(CreateBookRequest req) {
        String cid = UUID.randomUUID().toString();
        return graph.create(req)
                .flatMap(created ->
                        ts.writeEvent(new TsEventDto(cid, "BOOK_CREATE", created.id()))
                                .thenReturn(created)
                                .onErrorResume(ex ->
                                        graph.delete(created.id())
                                                .then(Mono.<BookDto>error(new RemoteCallException("Timeseries failed, rolled back in Graph", ex)))
                                )
                );
    }

    // 3) DELETE (SAGA: zapamti staru knjigu -> obriši u Graph -> loguj u TS; ako TS padne, VRATI knjigu u Graph)
    public Mono<Void> deleteBook(String id) {
        String cid = UUID.randomUUID().toString();

        return graph.getById(id)
                .switchIfEmpty(Mono.error(new RemoteCallException("Book not found: " + id)))
                .flatMap(existing ->
                        graph.delete(id)
                                .then(ts.writeEvent(new TsEventDto(cid, "BOOK_DELETE", id)))
                                .onErrorResume(ex ->
                                        // kompenzacija: vrati knjigu (re-create) ako TS padne
                                        graph.create(new CreateBookRequest(existing.title(), existing.year(), existing.authors(), existing.genres()))
                                                .then(Mono.<Void>error(new RemoteCallException("Timeseries failed, restored book in Graph", ex)))
                                )
                );
    }
}
