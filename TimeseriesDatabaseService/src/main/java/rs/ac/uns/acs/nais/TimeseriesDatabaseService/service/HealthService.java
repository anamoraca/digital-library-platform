package rs.ac.uns.acs.nais.TimeseriesDatabaseService.service;

import com.influxdb.client.InfluxDBClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.HealthCheckEvent;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.BuildInfo;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.HealthInfo;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.ServiceStatus;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository.HealthRepository;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthService {

    private final InfluxDBClient influx;
    private final HealthRepository repo;

    @Value("${spring.influx.url:}")
    private String influxUrl;

    @Value("${influx.org:}")
    private String org;

    @Value("${influx.bucket:}")
    private String bucket;

    @Value("${app.name:timeseries-service}")
    private String appName;

    @Value("${app.version:0.0.1-SNAPSHOT}")
    private String appVersion;

    @Value("${app.buildTime:unknown}")
    private String buildTime;

    @Value("${app.gitSha:unknown}")
    private String gitSha;

    // --- ping/info ---

    public HealthInfo health() {
        boolean reachable;
        try { reachable = influx.ping(); } catch (Exception e) { reachable = false; }
        String status = reachable ? "UP" : "DOWN";
        return new HealthInfo(status, Instant.now(), influxUrl, org, bucket, reachable);
    }

    public BuildInfo info() {
        return new BuildInfo(appName, appVersion, buildTime, gitSha);
    }

    // --- CRUD health_check ---

    public void insert(HealthCheckEvent e) { repo.insertHealthCheck(e); }
    public void insertBatch(List<HealthCheckEvent> events) { repo.insertHealthChecksBatch(events); }

    public void deleteRange(Instant from, Instant to, String serviceName, String source, String status) {
        repo.deleteHealthChecks(from, to, serviceName, source, status);
    }

    public List<TimeSeriesPoint<Long>> downOverview(String serviceName, String range, String interval) {
        return repo.fetchDownOverview(serviceName, range, interval);
    }

    public List<ServiceStatus> latestStatus(String serviceName) {
        return repo.fetchLatestStatusPerService(serviceName);
    }
}
