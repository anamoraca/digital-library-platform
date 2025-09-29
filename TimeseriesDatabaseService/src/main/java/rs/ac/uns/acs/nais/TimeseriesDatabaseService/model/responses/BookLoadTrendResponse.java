package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses;

import java.util.List;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;

public record BookLoadTrendResponse(
        String bookId,
        String range,      // "7d"
        String interval,   // "1h"
        List<TimeSeriesPoint<Double>> points
) { }