package rs.ac.uns.acs.nais.TimeseriesDatabaseService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.enums.*;

import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.ErrorTypeCount;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository.ErrorAnalyticsRepository;

import java.time.Instant;   // <--- dodaj
import java.util.List;

@Service
@RequiredArgsConstructor
public class ErrorAnalyticsService {

    private final ErrorAnalyticsRepository repo;

    // INSERT
    public void insert(ErrorLogEvent event) {
        repo.insertError(event);
    }

    public void insertBatch(List<ErrorLogEvent> events) {
        repo.insertErrorsBatch(events);
    }

    // DELETE
    public void deleteRange(Instant from, Instant to, String serviceName, String errorType) {
        repo.deleteErrors(from, to, serviceName, errorType);
    }

    // READ
    public List<TimeSeriesPoint<Long>> overview(String service, String range, String interval) {
        return repo.fetchErrorsOverview(service, range, interval);
    }

    public List<ErrorTypeCount> byType(String service, String range, int limit) {
        return repo.fetchErrorsByType(service, range, limit);
    }


    public List<ErrorLogEvent> search(String service, String errorType, Instant from, Instant to, int limit) {
        if (limit <= 0) limit = 100;
        return repo.findErrors(service, errorType, from, to, limit);
    }


}
