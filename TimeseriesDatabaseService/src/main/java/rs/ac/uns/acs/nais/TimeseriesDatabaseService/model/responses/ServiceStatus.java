package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses;

import java.time.Instant;

public record ServiceStatus(
        String serviceName,
        String status,        // "UP"/"DOWN"
        Double latencyMs,     // poslednja izmerena latencija ako postoji
        Instant ts            // vreme poslednjeg health check-a
) { }
