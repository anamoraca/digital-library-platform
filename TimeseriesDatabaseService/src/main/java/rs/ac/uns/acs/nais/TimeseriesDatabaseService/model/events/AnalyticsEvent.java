package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events;

import java.time.Instant;
import java.util.Map;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.enums.EventType;

public record AnalyticsEvent(
        EventType type,
        Instant ts,
        Map<String, String> tags,
        Map<String, Object> fields,
        String idempotencyKey
) { }