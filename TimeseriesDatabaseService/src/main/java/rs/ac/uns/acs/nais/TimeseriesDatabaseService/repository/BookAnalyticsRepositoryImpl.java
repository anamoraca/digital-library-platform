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
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.enums.*;
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




    public List<BookEvent> findEventsByBook(
            String bookId,
            Instant from,
            Instant to,
            BookEventType event,
            BookFormat format,
            int limit
    ) {
        if (limit <= 0) limit = 100;

        // Influx akceptira RFC3339: 2025-10-01T12:00:00Z
        String start = (from != null) ? "time(v: " + from + ")" : "-30d";
        String stop  = (to   != null) ? "time(v: " + to   + ")" : "now()";

        StringBuilder flux = new StringBuilder()
                .append("from(bucket: \"").append(bucket).append("\")")
                .append(" |> range(start: ").append(start).append(", stop: ").append(stop).append(")")
                .append(" |> filter(fn: (r) => r._measurement == \"book_event\")")
                // ⬇️ ključna razlika: snake_case tag
                .append(" |> filter(fn: (r) => r.book_id == \"").append(bookId).append("\")");

        if (event != null) {
            flux.append(" |> filter(fn: (r) => r.event == \"").append(event.name()).append("\")");
        }
        if (format != null) {
            flux.append(" |> filter(fn: (r) => r.format == \"").append(format.name()).append("\")");
        }

        // (opciono) zadrži samo kolone koje trebaju pivotu i parsiranju – smanjuje šum
        flux.append(" |> keep(columns: [\"_time\",\"_field\",\"_value\",\"book_id\",\"user_id\",\"event\",\"format\"])");

        // pivot da dobijemo load_ms i delta_pages u istom redu
        flux.append(" |> pivot(rowKey:[\"_time\",\"book_id\",\"user_id\",\"event\",\"format\"], columnKey:[\"_field\"], valueColumn:\"_value\")")
                .append(" |> sort(columns:[\"_time\"], desc: true)")
                .append(" |> limit(n: ").append(limit).append(")");

        QueryApi qa = influx.getQueryApi();
        List<FluxTable> tables = qa.query(flux.toString(), org);

        List<BookEvent> out = new ArrayList<>();
        for (FluxTable t : tables) {
            for (FluxRecord r : t.getRecords()) {
                // ⬇️ čitaj snake_case tagove
                String userId  = (String) r.getValueByKey("user_id");
                String _bookId = (String) r.getValueByKey("book_id");
                String fmt     = (String) r.getValueByKey("format");
                String evt     = (String) r.getValueByKey("event");

                Double loadMs = null;
                Integer deltaPages = null;

                Object lm = r.getValueByKey("load_ms");
                Object dp = r.getValueByKey("delta_pages");
                if (lm instanceof Number n) loadMs = n.doubleValue();
                if (dp instanceof Number n) deltaPages = n.intValue();

                Instant ts = r.getTime(); // _time

                out.add(new BookEvent(
                        userId,
                        _bookId,
                        fmt != null ? BookFormat.valueOf(fmt) : null,
                        evt != null ? BookEventType.valueOf(evt) : null,
                        loadMs,
                        deltaPages,
                        ts,
                        null // idempotencyKey – dodaj ako ga čuvaš kao tag/field
                ));
            }
        }
        return out;
    }



    @Override
    public void updateEventTypeForLatestByBook(String bookId, BookEventType toEvent) {
        // 1) Nađi NAJSKORIJI event za dati bookId (pivot da dobijemo oba polja u istom redu)
        String flux = """
        from(bucket: "%s")
          |> range(start: -30d)
          |> filter(fn: (r) => r._measurement == "book_event")
          |> filter(fn: (r) => r.book_id == "%s")
          |> keep(columns: ["_time","_field","_value","book_id","user_id","event","format"])
          |> pivot(rowKey:["_time","book_id","user_id","event","format"], columnKey:["_field"], valueColumn:"_value")
          |> sort(columns: ["_time"], desc: true)
          |> limit(n: 1)
        """.formatted(bucket, escapeForFlux(bookId));

        QueryApi qa = influx.getQueryApi();
        List<FluxTable> tables = qa.query(flux, org);

        FluxRecord rec = null;
        if (!tables.isEmpty() && !tables.get(0).getRecords().isEmpty()) {
            rec = tables.get(0).getRecords().get(0);
        }
        if (rec == null) {
            // nema ništa za izmijeniti
            return;
        }

        // Izvuci stare vrijednosti
        Instant ts = rec.getTime();
        String userId   = (String) rec.getValueByKey("user_id");
        String fmtTag   = (String) rec.getValueByKey("format");
        String evtTag   = (String) rec.getValueByKey("event");

        Double loadMs = null;
        Integer deltaPages = null;
        Object lm = rec.getValueByKey("load_ms");
        Object dp = rec.getValueByKey("delta_pages");
        if (lm instanceof Number n) loadMs = n.doubleValue();
        if (dp instanceof Number n) deltaPages = n.intValue();

        BookFormat fmt = null;
        try { if (fmtTag != null) fmt = BookFormat.valueOf(fmtTag); } catch (Exception ignore) {}

        // 2) Obriši TAČNO taj point (1ns prozor + precizan predicate)
        OffsetDateTime start = ts.atOffset(ZoneOffset.UTC);
        OffsetDateTime stop  = start.plusNanos(1); // najmanji mogući opseg

        StringBuilder predicate = new StringBuilder("_measurement=\"book_event\"");
        predicate.append(" AND book_id=\"").append(escapePred(bookId)).append("\"");
        if (userId != null && !userId.isBlank()) {
            predicate.append(" AND user_id=\"").append(escapePred(userId)).append("\"");
        }
        if (evtTag != null && !evtTag.isBlank()) {
            predicate.append(" AND event=\"").append(escapePred(evtTag)).append("\"");
        }
        if (fmtTag != null && !fmtTag.isBlank()) {
            predicate.append(" AND format=\"").append(escapePred(fmtTag)).append("\"");
        }

        influx.getDeleteApi().delete(start, stop, predicate.toString(), bucket, org);

        // 3) Upisi isti point sa novim 'event' tagom i ISTIM timestampom
        BookEvent updated = new BookEvent(
                userId,
                bookId,
                fmt,
                toEvent,      // promijenjen event
                loadMs,
                deltaPages,
                ts,           // čuvamo isti TS
                null          // idempotencyKey ako koristiš – ovdje ga ne čuvamo
        );

        // Koristimo postojeći toLineProtocol (uzima e.ts ako je postavljen)
        WriteApiBlocking w = influx.getWriteApiBlocking();
        w.writeRecord(bucket, org, WritePrecision.NS, toLineProtocol(updated));
    }










}
