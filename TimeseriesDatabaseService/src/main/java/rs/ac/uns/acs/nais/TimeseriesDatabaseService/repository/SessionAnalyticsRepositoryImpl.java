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
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.AppSessionEvent;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.enums.SessionEventType;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository.SessionAnalyticsRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Repository
@RequiredArgsConstructor
public class SessionAnalyticsRepositoryImpl implements SessionAnalyticsRepository {

    private final InfluxDBClient influx;

    @Value("${influx.bucket}")
    private String bucket;

    @Value("${influx.org}")
    private String org;

    // ------------- CREATE -------------

    @Override
    public void insertSession(AppSessionEvent e) {
        WriteApiBlocking w = influx.getWriteApiBlocking();
        w.writeRecord(bucket, org, WritePrecision.NS, toLineProtocol(e));
    }

    @Override
    public void insertSessionsBatch(List<AppSessionEvent> events) {
        if (events == null || events.isEmpty()) return;
        StringJoiner sj = new StringJoiner("\n");
        for (AppSessionEvent e : events) sj.add(toLineProtocol(e));
        influx.getWriteApiBlocking().writeRecord(bucket, org, WritePrecision.NS, sj.toString());
    }

    private String toLineProtocol(AppSessionEvent e) {
        // measurement: app_session
        // tags: user_id, device, app_version, event, (opciono) idempotencyKey
        // fields: duration_sec (samo za END događaj)
        // timestamp: ns
        Instant ts = (e.ts() != null) ? e.ts() : Instant.now();
        long ns = ts.toEpochMilli() * 1_000_000L;

        StringBuilder sb = new StringBuilder();
        sb.append("app_session")
                .append(",user_id=").append(escape(e.userId()))
                .append(",device=").append(escape(e.device()))
                .append(",app_version=").append(escape(e.appVersion()))
                .append(",event=").append(e.event() != null ? e.event().name() : "START");
        if (e.idempotencyKey() != null && !e.idempotencyKey().isBlank()) {
            sb.append(",idempotencyKey=").append(escape(e.idempotencyKey()));
        }
        // fields
        sb.append(" ");
        if (e.event() == SessionEventType.END && e.durationSec() != null) {
            sb.append("duration_sec=").append(e.durationSec());
        } else {
            // Influx line protocol zahteva makar jedno polje; upišemo flag=1
            sb.append("flag=1i");
        }
        sb.append(" ").append(ns);

        return sb.toString();
    }

    // ------------- DELETE -------------

    @Override
    public void deleteSessions(Instant from, Instant to, String userId, String device, String appVersion) {
        DeleteApi del = influx.getDeleteApi();

        StringBuilder predicate = new StringBuilder("_measurement=\"app_session\"");
        if (userId != null && !userId.isBlank()) {
            predicate.append(" AND user_id=\"").append(escapePred(userId)).append("\"");
        }
        if (device != null && !device.isBlank()) {
            predicate.append(" AND device=\"").append(escapePred(device)).append("\"");
        }
        if (appVersion != null && !appVersion.isBlank()) {
            predicate.append(" AND app_version=\"").append(escapePred(appVersion)).append("\"");
        }

        // KONVERZIJA Instant -> OffsetDateTime (UTC)
        OffsetDateTime start = from.atOffset(ZoneOffset.UTC);
        OffsetDateTime stop  = to.atOffset(ZoneOffset.UTC);

        // Influx v2 DeleteApi: delete(start, stop, predicate, bucket, org)
        del.delete(start, stop, predicate.toString(), bucket, org);
    }


    // ------------- READ -------------

    @Override
    public long fetchActiveUsers(String window) {
        String flux = """
            from(bucket: "%s")
              |> range(start: -%s)
              |> filter(fn: (r) => r._measurement == "app_session" and r.event == "START")
              |> keep(columns: ["user_id"])
              |> distinct(column: "user_id")
              |> count()
            """.formatted(bucket, window);

        QueryApi qa = influx.getQueryApi();
        List<FluxTable> tables = qa.query(flux, org);
        long total = 0L;
        for (FluxTable t : tables) {
            for (FluxRecord r : t.getRecords()) {
                total += toLong(r.getValue());
            }
        }
        return total;
    }

    @Override
    public List<Double> fetchDurations(String range) {
        String flux = """
            from(bucket: "%s")
              |> range(start: -%s)
              |> filter(fn: (r) => r._measurement == "app_session" and r.event == "END")
              |> filter(fn: (r) => r._field == "duration_sec")
              |> keep(columns: ["_value"])
            """.formatted(bucket, range);

        QueryApi qa = influx.getQueryApi();
        List<FluxTable> tables = qa.query(flux, org);
        List<Double> out = new ArrayList<>();
        for (FluxTable t : tables) {
            for (FluxRecord r : t.getRecords()) {
                out.add(toDouble(r.getValue()));
            }
        }
        return out;
    }

    // ------------- helpers -------------

    private static long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return 0L; }
    }

    private static double toDouble(Object v) {
        if (v == null) return 0d;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return 0d; }
    }

    private static String escape(String s) {
        if (s == null) return "";
        // escape za tag vrednosti u Line Protocol: razmak, zarez, jednako
        return s.replace(" ", "\\ ").replace(",", "\\,").replace("=", "\\=");
    }

    private static String escapePred(String s) {
        if (s == null) return "";
        // za predicate string (nema potrebe da se escape-uje zarez i =, ali escape-ujemo navodnike)
        return s.replace("\"", "\\\"");
    }
}
