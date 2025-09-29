package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses;

import java.time.Instant;

public record HealthInfo(
        String status,        // "UP" / "DOWN"
        Instant timestamp,    // vreme generisanja odgovora
        String influxUrl,
        String org,
        String bucket,
        boolean influxReachable
) { }
