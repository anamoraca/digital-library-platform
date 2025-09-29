package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.histogram;

import java.util.List;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.histogram.HistogramBin;

public record HistogramResponse(
        String range,
        int buckets,
        List<HistogramBin> bins
) { }