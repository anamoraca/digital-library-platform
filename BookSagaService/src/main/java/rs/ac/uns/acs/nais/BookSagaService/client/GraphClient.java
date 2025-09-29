package rs.ac.uns.acs.nais.BookSagaService.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import rs.ac.uns.acs.nais.BookSagaService.dto.BookDto;
import rs.ac.uns.acs.nais.BookSagaService.dto.CreateBookRequest;


@Component
public class GraphClient {
    private final WebClient graph;

    // Pretpostavka: GraphDatabaseService ekspozira /api/graph/books
    // Ako su rute drugačije – promeni samo stringove ispod.
    public GraphClient(WebClient graphWebClient) {
        this.graph = graphWebClient;
    }

    public Mono<BookDto> getById(String id) {
        return graph.get()
                .uri("/api/graph/books/{id}", id)
                .retrieve()
                .bodyToMono(BookDto.class);
    }

    public Mono<BookDto> create(CreateBookRequest req) {
        return graph.post()
                .uri("/api/graph/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(BookDto.class);
    }

    public Mono<Void> delete(String id) {
        return graph.delete()
                .uri("/api/graph/books/{id}", id)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
