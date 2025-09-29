package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses;

public record ApiLatencySummary(
        String service,
        String route,
        String range,   // npr. "24h"
        double p50Ms,
        double p95Ms,
        double p99Ms,
        long count
) { }