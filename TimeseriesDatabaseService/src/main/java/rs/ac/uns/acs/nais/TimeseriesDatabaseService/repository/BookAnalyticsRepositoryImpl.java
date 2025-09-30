package rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxTable;
import com.influxdb.query.FluxRecord;

import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.BookEvent;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.BookLoadTrendResponse;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.TopBookMetric;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

@Repository
@RequiredArgsConstructor
public class BookAnalyticsRepositoryImpl implements BookAnalyticsRepository {

    private final InfluxDBClient influx;

    @Value("${influx.bucket}")
    private String bucket;

    @Value("${influx.org}")
    private String org;

    // ---------- CREATE ----------

    @Override
    public void insertBookEvent(BookEvent e) {
        WriteApiBlocking w = influx.getWriteApiBlocking();
        w.writeRecord(bucket, org, WritePrecision.NS, toLineProtocol(e));
    }

    @Override
    public void insertBookEventsBatch(List<BookEvent> events) {
        if (events == null || events.isEmpty()) return;
        StringJoiner sj = new StringJoiner("\n");
        for (BookEvent e : events) sj.add(toLineProtocol(e));
        influx.getWriteApiBlocking().writeRecord(bucket, org, WritePrecision.NS, sj.toString());
    }

    private String toLineProtocol(BookEvent e) {
        Instant ts = (e.ts() != null) ? e.ts() : Instant.now();
        long ns = ts.toEpochMilli() * 1_000_000L;

        StringBuilder sb = new StringBuilder();
        sb.append("book_event")
                .append(",user_id=").append(escape(e.userId()))
                .append(",book_id=").append(escape(e.bookId()))
                .append(",format=").append(e.format() != null ? escape(e.format().name()) : "UNKNOWN")
                .append(",event=").append(e.event() != null ? escape(e.event().name()) : "OPENED");
        if (e.idempotencyKey() != null && !e.idempotencyKey().isBlank()) {
            sb.append(",idempotencyKey=").append(escape(e.idempotencyKey()));
        }

        boolean wroteAnyField = false;
        StringBuilder fields = new StringBuilder();

        if (e.loadMs() != null) {
            fields.append("load_ms=").append(e.loadMs());
            wroteAnyField = true;
        }
        if (e.deltaPages() != null) {
            if (wroteAnyField) fields.append(",");
            fields.append("delta_pages=").append(e.deltaPages());
            wroteAnyField = true;
        }
        if (!wroteAnyField) {
            fields.append("flag=1i");
        }

        sb.append(" ").append(fields)
                .append(" ").append(ns);

        return sb.toString();
    }

    // ---------- DELETE ----------

    @Override
    public void deleteBookEvents(Instant from, Instant to,
                                 String bookId, String userId, String format, String event) {
        DeleteApi del = influx.getDeleteApi();

        StringBuilder predicate = new StringBuilder("_measurement=\"book_event\"");
        if (bookId != null && !bookId.isBlank()) {
            predicate.append(" AND book_id=\"").append(escapePred(bookId)).append("\"");
        }
        if (userId != null && !userId.isBlank()) {
            predicate.append(" AND user_id=\"").append(escapePred(userId)).append("\"");
        }
        if (format != null && !format.isBlank()) {
            predicate.append(" AND format=\"").append(escapePred(format)).append("\"");
        }
        if (event != null && !event.isBlank()) {
            predicate.append(" AND event=\"").append(escapePred(event)).append("\"");
        }

        OffsetDateTime start = from.atOffset(ZoneOffset.UTC);
        OffsetDateTime stop  = to.atOffset(ZoneOffset.UTC);

        del.delete(start, stop, predicate.toString(), bucket, org);
    }

    // ---------- READ ----------

    @Override
    public BookLoadTrendResponse fetchBookLoadTrend(String bookId, String range, String interval) {
        // + sort po vremenu unutar Flux-a
        String flux = """
            from(bucket: "%s")
              |> range(start: -%s)
              |> filter(fn: (r) => r._measurement == "book_event")
              |> filter(fn: (r) => r.book_id == "%s" and r.event == "OPENED")
              |> filter(fn: (r) => r._field == "load_ms")
              |> aggregateWindow(every: %s, fn: mean, createEmpty: false)
              |> sort(columns: ["_time"], desc: false)
              |> keep(columns: ["_time","_value"])
            """.formatted(bucket, range, escapeForFlux(bookId), interval);

        List<TimeSeriesPoint<Double>> points = queryTimeSeriesDouble(flux);
        return new BookLoadTrendResponse(bookId, range, interval, points);
    }

    @Override
    public List<TimeSeriesPoint<Double>> fetchProgressRate(String bookId, String range, String interval) {
        // + sort po vremenu unutar Flux-a
        String flux = """
            from(bucket: "%s")
              |> range(start: -%s)
              |> filter(fn: (r) => r._measurement == "book_event")
              |> filter(fn: (r) => r.book_id == "%s" and r.event == "PROGRESS")
              |> filter(fn: (r) => r._field == "delta_pages")
              |> aggregateWindow(every: %s, fn: mean, createEmpty: false)
              |> sort(columns: ["_time"], desc: false)
              |> keep(columns: ["_time","_value"])
            """.formatted(bucket, range, escapeForFlux(bookId), interval);

        return queryTimeSeriesDouble(flux);
    }

    @Override
    public List<TopBookMetric> fetchTopBooks(String range, String metric, int limit) {
        metric = metric.toLowerCase(Locale.ROOT);

        String flux;
        switch (metric) {
            case "open_count" -> {
                // grupisanje + count + sort desc + limit u Flux-u
                flux = """
                    from(bucket: "%s")
                      |> range(start: -%s)
                      |> filter(fn: (r) => r._measurement == "book_event")
                      |> filter(fn: (r) => r.event == "OPENED")
                      |> filter(fn: (r) => r._field == "load_ms")
                      |> group(columns: ["book_id"])
                      |> count(column: "_value")
                      |> rename(columns: {_value: "metric"})
                      |> sort(columns: ["metric"], desc: true)
                      |> limit(n: %d)
                      |> keep(columns: ["book_id", "metric"])
                    """.formatted(bucket, range, limit);
            }
            case "read_time" -> {
                // sum(delta_pages) + sort desc + limit u Flux-u
                flux = """
                    from(bucket: "%s")
                      |> range(start: -%s)
                      |> filter(fn: (r) => r._measurement == "book_event")
                      |> filter(fn: (r) => r.event == "PROGRESS")
                      |> filter(fn: (r) => r._field == "delta_pages")
                      |> group(columns: ["book_id"])
                      |> sum(column: "_value")
                      |> rename(columns: {_value: "metric"})
                      |> sort(columns: ["metric"], desc: true)
                      |> limit(n: %d)
                      |> keep(columns: ["book_id", "metric"])
                    """.formatted(bucket, range, limit);
            }
            default -> throw new IllegalArgumentException("Unknown metric: " + metric);
        }

        QueryApi qa = influx.getQueryApi();
        List<FluxTable> tables = qa.query(flux, org);
        List<TopBookMetric> out = new ArrayList<>();

        for (FluxTable t : tables) {
            for (FluxRecord r : t.getRecords()) {
                String bId = str(r.getValueByKey("book_id"));
                // Čitaj eksplicitno "metric" kolonu (pošto smo _value -> metric u Flux-u)
                double value = dbl(r.getValueByKey("metric"));
                out.add(new TopBookMetric(bId, value));
            }
        }
        // više ne sortiramo/limitiramo u Javi — sve je već urađeno u Flux-u
        return out;
    }

    // ---------- helpers ----------

    private List<TimeSeriesPoint<Double>> queryTimeSeriesDouble(String flux) {
        QueryApi qa = influx.getQueryApi();
        List<FluxTable> tables = qa.query(flux, org);
        List<TimeSeriesPoint<Double>> points = new ArrayList<>();
        for (FluxTable t : tables) {
            for (FluxRecord r : t.getRecords()) {
                Instant ts = (Instant) r.getTime();
                double val = dbl(r.getValue()); // ovdje _value ostaje jer keep ostavlja "_value"
                points.add(new TimeSeriesPoint<>(ts, val));
            }
        }
        // zadržavam lokalno sortiranje kao safety-net (Flux već sortira)
        points.sort(Comparator.comparing(TimeSeriesPoint::time));
        return points;
    }

    private static double dbl(Object v) {
        if (v == null) return 0d;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return 0d; }
    }

    private static String str(Object v) {
        return v == null ? "" : v.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace(" ", "\\ ").replace(",", "\\,").replace("=", "\\=");
    }

    private static String escapePred(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }

    private static String escapeForFlux(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
