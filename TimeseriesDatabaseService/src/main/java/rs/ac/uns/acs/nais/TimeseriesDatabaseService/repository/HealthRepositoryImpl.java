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
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.HealthCheckEvent;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.ServiceStatus;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository.HealthRepository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

@Repository
@RequiredArgsConstructor
public class HealthRepositoryImpl implements HealthRepository {

    private final InfluxDBClient influx;

    @Value("${influx.bucket}")
    private String bucket;

    @Value("${influx.org}")
    private String org;

    // ------------ CREATE ------------

    @Override
    public void insertHealthCheck(HealthCheckEvent e) {
        WriteApiBlocking w = influx.getWriteApiBlocking();
        w.writeRecord(bucket, org, WritePrecision.NS, toLineProtocol(e));
    }

    @Override
    public void insertHealthChecksBatch(List<HealthCheckEvent> events) {
        if (events == null || events.isEmpty()) return;
        StringJoiner sj = new StringJoiner("\n");
        for (HealthCheckEvent e : events) sj.add(toLineProtocol(e));
        influx.getWriteApiBlocking().writeRecord(bucket, org, WritePrecision.NS, sj.toString());
    }

    private String toLineProtocol(HealthCheckEvent e) {
        // measurement: health_check
        // tags: service, source, status, (opciono) idempotencyKey
        // fields: latency_ms (double) ili flag=1i ako nema
        // ts: ns
        Instant ts = (e.ts() != null) ? e.ts() : Instant.now();
        long ns = ts.toEpochMilli() * 1_000_000L;

        StringBuilder sb = new StringBuilder();
        sb.append("health_check")
                .append(",service=").append(escapeTag(e.serviceName()))
                .append(",source=").append(escapeTag(e.source()))
                .append(",status=").append(escapeTag(e.status() == null ? "UNKNOWN" : e.status()));
        if (e.idempotencyKey() != null && !e.idempotencyKey().isBlank()) {
            sb.append(",idempotencyKey=").append(escapeTag(e.idempotencyKey()));
        }

        sb.append(" ");
        if (e.latencyMs() != null) {
            sb.append("latency_ms=").append(e.latencyMs());
        } else {
            sb.append("flag=1i");
        }
        sb.append(" ").append(ns);

        return sb.toString();
    }

    // ------------ DELETE ------------

    @Override
    public void deleteHealthChecks(Instant from, Instant to, String serviceName, String source, String status) {
        DeleteApi del = influx.getDeleteApi();

        StringBuilder predicate = new StringBuilder("_measurement=\"health_check\"");
        if (serviceName != null && !serviceName.isBlank()) {
            predicate.append(" AND service=\"").append(escapePred(serviceName)).append("\"");
        }
        if (source != null && !source.isBlank()) {
            predicate.append(" AND source=\"").append(escapePred(source)).append("\"");
        }
        if (status != null && !status.isBlank()) {
            predicate.append(" AND status=\"").append(escapePred(status)).append("\"");
        }

        OffsetDateTime start = from.atOffset(ZoneOffset.UTC);
        OffsetDateTime stop  = to.atOffset(ZoneOffset.UTC);

        del.delete(start, stop, predicate.toString(), bucket, org);
    }

    // ------------ READ ------------

    @Override
    public List<TimeSeriesPoint<Long>> fetchDownOverview(String serviceName, String range, String interval) {
        String serviceFilter = (serviceName == null || serviceName.isBlank())
                ? ""
                : "  |> filter(fn: (r) => r.service == \"" + escapeFlux(serviceName) + "\")\n";

        String flux = """
            from(bucket: "%s")
              |> range(start: -%s)
              |> filter(fn: (r) => r._measurement == "health_check")
              |> filter(fn: (r) => r.status == "DOWN")
              |> map(fn: (r) => ({ r with _value: 1.0 }))
            %s  |> aggregateWindow(every: %s, fn: sum, createEmpty: false)
              |> keep(columns: ["_time","_value"])
            """.formatted(bucket, range, serviceFilter, interval);

        QueryApi qa = influx.getQueryApi();
        List<FluxTable> tables = qa.query(flux, org);
        List<TimeSeriesPoint<Long>> out = new ArrayList<>();
        for (FluxTable t : tables) {
            for (FluxRecord r : t.getRecords()) {
                Instant time = (Instant) r.getTime();
                long val = toLong(r.getValue());
                out.add(new TimeSeriesPoint<>(time, val));
            }
        }
        out.sort(Comparator.comparing(TimeSeriesPoint::time));
        return out;
    }

    @Override
    public List<ServiceStatus> fetchLatestStatusPerService(String serviceName) {
        String serviceFilter = (serviceName == null || serviceName.isBlank())
                ? ""
                : "  |> filter(fn: (r) => r.service == \"" + escapeFlux(serviceName) + "\")\n";

        String flux = """
            from(bucket: "%s")
              |> range(start: -30d)
              |> filter(fn: (r) => r._measurement == "health_check")
            %s  |> group(columns: ["service"])
              |> sort(columns: ["_time"], desc: true)
              |> first()  // posle sortiranja desc, first() je poslednji zapis
              |> keep(columns: ["service", "status", "_time", "_field", "_value"])
            """.formatted(bucket, serviceFilter);

        QueryApi qa = influx.getQueryApi();
        List<FluxTable> tables = qa.query(flux, org);
        List<ServiceStatus> out = new ArrayList<>();

        for (FluxTable t : tables) {
            for (FluxRecord r : t.getRecords()) {
                String svc = str(r.getValueByKey("service"));
                String status = str(r.getValueByKey("status"));
                Instant ts = (Instant) r.getTime();
                Double latency = null;
                // ako je red došao iz polja latency_ms, _value će biti latencija
                Object field = r.getValueByKey("_field");
                if (field != null && "latency_ms".equals(field.toString())) {
                    Object v = r.getValue();
                    if (v instanceof Number n) latency = n.doubleValue();
                    else try { latency = Double.parseDouble(v.toString()); } catch (Exception ignored) {}
                }
                out.add(new ServiceStatus(svc, status, latency, ts));
            }
        }
        return out;
    }

    // ------------ helpers ------------

    private static long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return 0L; }
    }

    private static String str(Object v) { return v == null ? "" : v.toString(); }

    private static String escapeTag(String s) {
        if (s == null) return "";
        return s.replace(" ", "\\ ").replace(",", "\\,").replace("=", "\\=");
    }

    private static String escapePred(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }

    private static String escapeFlux(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
