package rs.ac.uns.acs.nais.TimeseriesDatabaseService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.AppSessionEvent;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.histogram.HistogramBin;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.histogram.HistogramResponse;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.ActiveUsersDto;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository.SessionAnalyticsRepository;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionAnalyticsService {

    private final SessionAnalyticsRepository repo;

    // CREATE
    public void insert(AppSessionEvent event) {
        repo.insertSession(event);
    }

    public void insertBatch(List<AppSessionEvent> events) {
        if (events == null || events.isEmpty()) return;
        repo.insertSessionsBatch(events);
    }

    // DELETE
    public void deleteRange(Instant from, Instant to, String userId, String device, String appVersion) {
        repo.deleteSessions(from, to, userId, device, appVersion);
    }

    // READ
    public ActiveUsersDto activeUsers(String window) {
        long c = repo.fetchActiveUsers(window);
        return new ActiveUsersDto(window, c);
    }

    public HistogramResponse durationHistogram(String range, int buckets, int max) {
        List<Double> durations = repo.fetchDurations(range);

        double binSize = (double) max / buckets;
        List<HistogramBin> bins = new ArrayList<>(buckets);

        for (int i = 0; i < buckets; i++) {
            final double start = i * binSize;
            final double end = (i == buckets - 1) ? Double.POSITIVE_INFINITY : (i + 1) * binSize;
            final boolean lastBin = (i == buckets - 1);

            long count = durations.stream()
                    .filter(v -> lastBin ? v >= start : (v >= start && v < end))
                    .count();

            bins.add(new HistogramBin(start, end, count));
        }

        return new HistogramResponse(range, buckets, bins);
    }
}
