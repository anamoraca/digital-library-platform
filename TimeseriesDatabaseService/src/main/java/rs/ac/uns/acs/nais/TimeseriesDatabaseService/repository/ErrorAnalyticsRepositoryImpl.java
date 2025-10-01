package rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository.impl;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.enums.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.ErrorTypeCount;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository.ErrorAnalyticsRepository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

@Repository
@RequiredArgsConstructor
public class ErrorAnalyticsRepositoryImpl implements ErrorAnalyticsRepository {

    private final InfluxDBClient influx;

    @Value("${influx.bucket}")
    private String bucket;

    @Value("${influx.org}")
    private String org;

    // INSERT
    @Override
    public void insertError(ErrorLogEvent event) {
        WriteApiBlocking w = influx.getWriteApiBlocking();
        w.writeRecord(bucket, org, WritePrecision.NS, toLineProtocol(event));
    }

    @Override
    public void insertErrorsBatch(List<ErrorLogEvent> events) {
        if (events == null || events.isEmpty()) return;
        StringJoiner sj = new StringJoiner("\n");
        for (ErrorLogEvent e : events) sj.add(toLineProtocol(e));
        influx.getWriteApiBlocking().writeRecord(bucket, org, WritePrecision.NS, sj.toString());
    }

    private String toLineProtocol(ErrorLogEvent e) {
        Instant ts = (e.ts() != null) ? e.ts() : Instant.now();
        long ns = ts.toEpochMilli() * 1_000_000L;
        int cnt = (e.count() != null && e.count() > 0) ? e.count() : 1;

        StringBuilder sb = new StringBuilder();
        sb.append("error_log")
                .append(",service=").append(escape(e.service()))
                .append(",error_type=").append(escape(e.errorType()));
        if (e.idempotencyKey() != null && !e.idempotencyKey().isBlank()) {
            sb.append(",idempotencyKey=").append(escape(e.idempotencyKey()));
        }
        sb.append(" count=").append(cnt)
                .append(" ").append(ns);
        return sb.toString();
    }

    // DELETE
    @Override
    public void deleteErrors(Instant from, Instant to, String service, String errorType) {
        DeleteApi del = influx.getDeleteApi();
        StringBuilder predicate = new StringBuilder("_measurement=\"error_log\"");
        if (service != null && !service.isBlank()) {
            predicate.append(" AND service=\"").append(escapePred(service)).append("\"");
        }
        if (errorType != null && !errorType.isBlank()) {
            predicate.append(" AND error_type=\"").append(escapePred(errorType)).append("\"");
        }
        OffsetDateTime start = from.atOffset(ZoneOffset.UTC);
        OffsetDateTime stop  = to.atOffset(ZoneOffset.UTC);
        del.delete(start, stop, predicate.toString(), bucket, org);
    }

    // READ
    @Override
    public List<TimeSeriesPoint<Long>> fetchErrorsOverview(String service, String range, String interval) {
        String serviceFilter = (service == null || service.isBlank())
                ? ""
                : "  |> filter(fn: (r) => r.service == \"" + escape(service) + "\")\n";

        String flux = """
            from(bucket: "%s")
              |> range(start: -%s)
              |> filter(fn: (r) => r._measurement == "error_log")
              |> filter(fn: (r) => r._field == "count")
            %s  |> aggregateWindow(every: %s, fn: sum, createEmpty: false)
              |> keep(columns: ["_time","_value"])
            """.formatted(bucket, range, serviceFilter, interval);

        QueryApi qa = influx.getQueryApi();
        List<FluxTable> tables = qa.query(flux, org);
        List<TimeSeriesPoint<Long>> out = new ArrayList<>();
        for (FluxTable t : tables) {
            for (FluxRecord r : t.getRecords()) {
                Instant tstamp = (Instant) r.getTime();
                long val = toLong(r.getValue());
                out.add(new TimeSeriesPoint<>(tstamp, val));
            }
        }
        out.sort(Comparator.comparing(TimeSeriesPoint::time));
        return out;
    }

    @Override
    public List<ErrorTypeCount> fetchErrorsByType(String service, String range, int limit) {
        String serviceFilter = (service == null || service.isBlank())
                ? ""
                : "  |> filter(fn: (r) => r.service == \"" + escape(service) + "\")\n";

        String flux = """
            from(bucket: "%s")
              |> range(start: -%s)
              |> filter(fn: (r) => r._measurement == "error_log")
              |> filter(fn: (r) => r._field == "count")
            %s  |> group(columns: ["error_type"])
              |> sum(column: "_value")
              |> keep(columns: ["error_type","_value"])
            """.formatted(bucket, range, serviceFilter);

        QueryApi qa = influx.getQueryApi();
        List<FluxTable> tables = qa.query(flux, org);
        List<ErrorTypeCount> out = new ArrayList<>();
        for (FluxTable t : tables) {
            for (FluxRecord r : t.getRecords()) {
                String type = str(r.getValueByKey("error_type"));
                long count = toLong(r.getValue());
                out.add(new ErrorTypeCount(type, count));
            }
        }
        out.sort((a, b) -> Long.compare(b.count(), a.count()));
        return out.size() > limit ? out.subList(0, limit) : out;
    }

    // helpers
    private static long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return 0L; }
    }
    private static String str(Object v) { return v == null ? "" : v.toString(); }
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace(" ", "\\ ").replace(",", "\\,").replace("=", "\\=");
    }
    private static String escapePred(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }


    @Override
    public List<ErrorLogEvent> findErrors(String service, String errorType, Instant from, Instant to, int limit) {
        if (limit <= 0) limit = 100;

        String start = (from != null) ? "time(v: " + from + ")" : "-30d";
        String stop  = (to   != null) ? "time(v: " + to   + ")" : "now()";

        StringBuilder flux = new StringBuilder()
                .append("from(bucket: \"").append(bucket).append("\")")
                .append(" |> range(start: ").append(start).append(", stop: ").append(stop).append(")")
                .append(" |> filter(fn: (r) => r._measurement == \"error_log\")")
                .append(" |> filter(fn: (r) => r._field == \"count\")");

        if (service != null && !service.isBlank()) {
            flux.append(" |> filter(fn: (r) => r.service == \"").append(escape(service)).append("\")");
        }
        if (errorType != null && !errorType.isBlank()) {
            flux.append(" |> filter(fn: (r) => r.error_type == \"").append(escape(errorType)).append("\")");
        }

        flux.append(" |> keep(columns: [\"_time\",\"_value\",\"service\",\"error_type\",\"idempotencyKey\"])")
                .append(" |> sort(columns:[\"_time\"], desc: true)")
                .append(" |> limit(n: ").append(limit).append(")");

        QueryApi qa = influx.getQueryApi();
        List<FluxTable> tables = qa.query(flux.toString(), org);

        List<ErrorLogEvent> out = new ArrayList<>();
        for (FluxTable t : tables) {
            for (FluxRecord r : t.getRecords()) {
                Instant ts = r.getTime();
                long v = toLong(r.getValue()); // _value = count
                int count = (int) Math.min(Integer.MAX_VALUE, Math.max(0, v));

                String svc  = str(r.getValueByKey("service"));
                String type = str(r.getValueByKey("error_type"));
                String idem = str(r.getValueByKey("idempotencyKey"));
                if (idem.isBlank()) idem = null;

                out.add(new ErrorLogEvent(svc, type, count, ts, idem));
            }
        }
        return out;
    }


}
