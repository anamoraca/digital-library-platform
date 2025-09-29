package rs.ac.uns.acs.nais.BookSagaService.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import rs.ac.uns.acs.nais.BookSagaService.dto.TsEventDto;

@Component
public class TimeseriesClient {
   final WebClient ts;

    // Pretpostavka: TimeseriesDatabaseService ima rute /api/ts/events/books
    // (POST upis eventa, DELETE kompenzacija po correlationId).
    public TimeseriesClient(WebClient tsWebClient) { this.ts = tsWebClient; }

    public Mono<Void> writeEvent(TsEventDto ev) {
        return ts.post()
                .uri("/api/ts/events/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ev)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> deleteEvent(String correlationId) {
        return ts.delete()
                .uri("/api/ts/events/books/{cid}", correlationId)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
