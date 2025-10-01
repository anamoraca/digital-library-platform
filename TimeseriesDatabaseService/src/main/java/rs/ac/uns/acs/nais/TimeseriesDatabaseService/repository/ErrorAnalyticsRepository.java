package rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository;

import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.enums.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.ErrorTypeCount;

import java.time.Instant;
import java.util.List;

public interface ErrorAnalyticsRepository {

    // INSERT
    void insertError(ErrorLogEvent event);
    void insertErrorsBatch(List<ErrorLogEvent> events);

    // DELETE
    void deleteErrors(Instant from, Instant to, String service, String errorType);

    // READ
    List<TimeSeriesPoint<Long>> fetchErrorsOverview(String service, String range, String interval);
    List<ErrorTypeCount> fetchErrorsByType(String service, String range, int limit);

    List<ErrorLogEvent> findErrors(String service, String errorType, Instant from, Instant to, int limit);


}
