package rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository;

import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.BookEvent;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.BookLoadTrendResponse;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.TopBookMetric;

import java.time.Instant;
import java.util.List;

public interface BookAnalyticsRepository {

    void insertBookEvent(BookEvent e);
    void insertBookEventsBatch(List<BookEvent> events);

    void deleteBookEvents(Instant from, Instant to,
                          String bookId, String userId, String format, String event);
    BookLoadTrendResponse fetchBookLoadTrend(String bookId, String range, String interval);
    List<TimeSeriesPoint<Double>> fetchProgressRate(String bookId, String range, String interval);
    List<TopBookMetric> fetchTopBooks(String range, String metric, int limit);
}
