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
import java.util.Objects;

import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.report.PdfReportBuilder2;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.report.ReportPayload2;

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

    public byte[] generateReportPdfUsingExisting(
            String range,
            String interval,
            int limit,
            String complexBookId
    ) {
        var topOpen = repo.fetchTopBooks(range, "open_count", limit);
        var topRead = repo.fetchTopBooks(range, "read_time", limit);

        var loadTrendResp = repo.fetchBookLoadTrend(complexBookId, range, interval);
        var loadTrend = loadTrendResp.points();
        var progressTrend = repo.fetchProgressRate(complexBookId, range, interval);

        double avgLoad = avg(loadTrend.stream().map(TimeSeriesPoint::value).toList());
        double p95Load = percentile(loadTrend.stream().map(TimeSeriesPoint::value).toList(), 95);
        double avgProgressRate = avg(progressTrend.stream().map(TimeSeriesPoint::value).toList());

        return PdfReportBuilder2.buildReport(ReportPayload2.builder()
                .generatedAt(Instant.now())
                .range(range)
                .interval(interval)
                .limit(limit)
                .topOpen(topOpen)
                .topRead(topRead)
                .complexBookId(complexBookId)
                .loadTrend(loadTrend)
                .progressTrend(progressTrend)
                .avgLoad(avgLoad)
                .p95Load(p95Load)
                .avgProgressRate(avgProgressRate)
                .build());
    }

    private double avg(List<Double> xs) {
        if (xs == null || xs.isEmpty()) return 0.0;
        return xs.stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    private double percentile(List<Double> xs, int p) {
        if (xs == null || xs.isEmpty()) return 0.0;
        var s = xs.stream().filter(Objects::nonNull).sorted().toList();
        int idx = (int)Math.ceil((p/100.0) * s.size()) - 1;
        idx = Math.max(0, Math.min(idx, s.size()-1));
        return s.get(idx);
    }
}
