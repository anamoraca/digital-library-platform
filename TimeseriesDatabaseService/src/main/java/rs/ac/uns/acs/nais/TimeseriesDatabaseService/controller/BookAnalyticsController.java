package rs.ac.uns.acs.nais.TimeseriesDatabaseService.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.BookEvent;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.BookLoadTrendResponse;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.TopBookMetric;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.service.BookAnalyticsService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/analytics/books")
@RequiredArgsConstructor
@Validated
public class BookAnalyticsController {

    private final BookAnalyticsService service;

    // ---------- CREATE ----------

    /** Upis jednog BookEvent-a (OPENED/PROGRESS/CLOSED). */
    @PostMapping("/events")
    public ResponseEntity<Void> insertEvent(@RequestBody BookEvent event) {
        service.insertEvent(event);
        return ResponseEntity.accepted().build();
    }

    /** Batch upis više BookEvent-ova. */
    @PostMapping("/events/batch")
    public ResponseEntity<Void> insertEventsBatch(@RequestBody List<BookEvent> events) {
        service.insertEventsBatch(events);
        return ResponseEntity.accepted().build();
    }

    // ---------- DELETE ----------

    /**
     * Brisanje događaja po opsegu i opcionalnim filterima.
     * from/to su ISO-8601 u UTC (npr. 2025-09-27T00:00:00Z).
     * Filtri su po tagovima (book_id, user_id, format, event).
     */
    @DeleteMapping("/events")
    public ResponseEntity<Void> deleteEvents(
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(required = false) String bookId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String event
    ) {
        service.deleteEvents(from, to, bookId, userId, format, event);
        return ResponseEntity.noContent().build();
    }

    // ---------- READ (postojeće rute) ----------

    /** Trend prosečnog vremena učitavanja (load_ms) za konkretnu knjigu. */
    @GetMapping("/load-time/trend")
    public ResponseEntity<BookLoadTrendResponse> loadTimeTrend(
            @RequestParam @NotBlank String bookId,
            @RequestParam(defaultValue = "7d") String range,
            @RequestParam(defaultValue = "1h") String interval
    ) {
        return ResponseEntity.ok(service.loadTimeTrend(bookId, range, interval));
    }

    /** Tempo čitanja (prosečan delta_pages po intervalu) za konkretnu knjigu. */
    @GetMapping("/progress/rate")
    public ResponseEntity<List<TimeSeriesPoint<Double>>> progressRate(
            @RequestParam @NotBlank String bookId,
            @RequestParam(defaultValue = "7d") String range,
            @RequestParam(defaultValue = "1h") String interval
    ) {
        return ResponseEntity.ok(service.progressRate(bookId, range, interval));
    }

    /**
     * Top knjige za opseg po metrikama:
     *  - open_count (broj otvaranja)
     *  - read_time (aproksimacija: suma delta_pages)
     */
    @GetMapping("/top")
    public ResponseEntity<List<TopBookMetric>> topBooks(
            @RequestParam(defaultValue = "7d") String range,
            @RequestParam(defaultValue = "open_count") String metric,
            @RequestParam(defaultValue = "10") @Positive int limit
    ) {
        return ResponseEntity.ok(service.topBooks(range, metric, limit));
    }
}
