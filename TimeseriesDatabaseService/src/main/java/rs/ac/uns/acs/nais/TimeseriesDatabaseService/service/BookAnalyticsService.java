package rs.ac.uns.acs.nais.TimeseriesDatabaseService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.BookEvent;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.BookLoadTrendResponse;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.TopBookMetric;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository.BookAnalyticsRepository;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookAnalyticsService {

    private final BookAnalyticsRepository repo;

    // CREATE
    public void insertEvent(BookEvent e) {
        repo.insertBookEvent(e);
    }

    public void insertEventsBatch(List<BookEvent> events) {
        if (events == null || events.isEmpty()) return;
        repo.insertBookEventsBatch(events);
    }

    // DELETE
    public void deleteEvents(Instant from, Instant to, String bookId, String userId, String format, String event) {
        repo.deleteBookEvents(from, to, bookId, userId, format, event);
    }

    // READ
    public BookLoadTrendResponse loadTimeTrend(String bookId, String range, String interval) {
        return repo.fetchBookLoadTrend(bookId, range, interval);
    }

    public List<TimeSeriesPoint<Double>> progressRate(String bookId, String range, String interval) {
        return repo.fetchProgressRate(bookId, range, interval);
    }

    public List<TopBookMetric> topBooks(String range, String metric, int limit) {
        return repo.fetchTopBooks(range, metric, limit);
    }
}
