package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common;

import java.time.Instant;

public record TimeSeriesPoint<T>(
        //TODO: sta je instant
        Instant time,
        T value
) { }