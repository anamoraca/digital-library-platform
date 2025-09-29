package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events;

import java.util.List;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.AnalyticsEvent;
public record AnalyticsBatch(
        List<AnalyticsEvent> events
) { }
