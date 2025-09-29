package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common;

public record HealthInfo(
        String status,    // "UP"/"DOWN"
        String influx,    // "UP"/"DOWN"
        String org,
        String bucket
) { }