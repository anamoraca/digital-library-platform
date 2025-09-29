package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events;

import java.time.Instant;

public record ErrorLogEvent(
        String service,
        String errorType,
        Integer count,
        Instant ts,
        String idempotencyKey
) { }