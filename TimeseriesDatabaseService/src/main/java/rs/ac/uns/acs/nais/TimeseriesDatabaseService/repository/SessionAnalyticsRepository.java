package rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository;

import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.AppSessionEvent;

import java.time.Instant;
import java.util.List;

public interface SessionAnalyticsRepository {

    // CREATE
    void insertSession(AppSessionEvent event);
    void insertSessionsBatch(List<AppSessionEvent> events);

    // DELETE
    void deleteSessions(Instant from, Instant to, String userId, String device, String appVersion);

    // READ
    long fetchActiveUsers(String window);
    List<Double> fetchDurations(String range);
}
