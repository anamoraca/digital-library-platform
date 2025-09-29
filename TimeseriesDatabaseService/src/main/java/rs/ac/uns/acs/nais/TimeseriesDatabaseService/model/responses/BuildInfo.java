package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses;

public record BuildInfo(
        String name,
        String version,
        String buildTime,
        String gitSha
) { }
