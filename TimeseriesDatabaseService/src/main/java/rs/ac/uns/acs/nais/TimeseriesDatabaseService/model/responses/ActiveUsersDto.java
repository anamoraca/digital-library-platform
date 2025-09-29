package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses;

public record ActiveUsersDto(
        String window, // npr. "15m"
        long count
) { }