package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events;

import java.time.Instant;

public record HealthCheckEvent(
        String serviceName,     // npr. "gateway"
        String source,          // npr. "internal", "probe", "monitor"
        String status,          // "UP" ili "DOWN"
        Double latencyMs,       // opcionalno
        Instant ts,             // ako null -> now
        String idempotencyKey   // opcionalno
) { }
