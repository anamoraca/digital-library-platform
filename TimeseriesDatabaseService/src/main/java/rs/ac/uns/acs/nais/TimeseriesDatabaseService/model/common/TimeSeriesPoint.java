package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common;

import java.time.Instant;

public record TimeSeriesPoint<T>(
        Instant time,
        T value
) { }