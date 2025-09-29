package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses;

public record SeedResult(
        int bookEventsInserted,
        int sessionEventsInserted,
        int errorLogsInserted
) { }