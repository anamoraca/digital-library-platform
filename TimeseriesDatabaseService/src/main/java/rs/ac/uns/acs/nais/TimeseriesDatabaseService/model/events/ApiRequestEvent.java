package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events;

import java.time.Instant;

public record ApiRequestEvent(
        String service,
        String route,
        String method,
        int status,
        Double latencyMs,
        Long bytesOut,
        Instant ts,
        String idempotencyKey
) { }