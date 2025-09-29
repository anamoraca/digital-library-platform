package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.histogram;

public record HistogramBin(
        double startInclusive,
        double endExclusive,
        long count
) { }