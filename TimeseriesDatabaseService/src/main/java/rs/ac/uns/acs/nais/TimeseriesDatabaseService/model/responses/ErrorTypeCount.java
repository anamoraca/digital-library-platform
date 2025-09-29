package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses;

public record ErrorTypeCount(
        String errorType,
        long count
) { }