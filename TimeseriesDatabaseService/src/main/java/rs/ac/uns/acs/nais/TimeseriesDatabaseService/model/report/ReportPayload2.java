package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.report;

import lombok.Builder;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.TopBookMetric;

import java.time.Instant;
import java.util.List;

@Builder
public record ReportPayload2(
        Instant generatedAt,
        String range,
        String interval,
        int limit,
        List<TopBookMetric> topOpen,
        List<TopBookMetric> topRead,
        String complexBookId,
        List<TimeSeriesPoint<Double>> loadTrend,
        List<TimeSeriesPoint<Double>> progressTrend,
        double avgLoad,
        double p95Load,
        double avgProgressRate
) {}
