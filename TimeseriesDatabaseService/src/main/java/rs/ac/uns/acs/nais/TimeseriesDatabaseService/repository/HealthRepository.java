package rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository;

import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.HealthCheckEvent;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.ServiceStatus;

import java.time.Instant;
import java.util.List;

public interface HealthRepository {

    // CREATE
    void insertHealthCheck(HealthCheckEvent e);
    void insertHealthChecksBatch(List<HealthCheckEvent> events);

    // DELETE
    void deleteHealthChecks(Instant from, Instant to, String serviceName, String source, String status);

    // READ
    List<TimeSeriesPoint<Long>> fetchDownOverview(String serviceName, String range, String interval);
    List<ServiceStatus> fetchLatestStatusPerService(String serviceName);
}
