package rs.ac.uns.acs.nais.TimeseriesDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.HealthCheckEvent;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.BuildInfo;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.HealthInfo;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.ServiceStatus;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.service.HealthService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService service;

    // --- ping/info ---

    @GetMapping("/health")
    public ResponseEntity<HealthInfo> health() {
        return ResponseEntity.ok(service.health());
    }

    @GetMapping("/info")
    public ResponseEntity<BuildInfo> info() {
        return ResponseEntity.ok(service.info());
    }

    // --- CRUD za health_check ---

    @PostMapping("/health/checks")
    public ResponseEntity<Void> insert(@RequestBody HealthCheckEvent e) {
        service.insert(e);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/health/checks/batch")
    public ResponseEntity<Void> insertBatch(@RequestBody List<HealthCheckEvent> events) {
        service.insertBatch(events);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/health/checks")
    public ResponseEntity<Void> delete(
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String status
    ) {
        service.deleteRange(from, to, serviceName, source, status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health/checks/overview")
    public ResponseEntity<List<TimeSeriesPoint<Long>>> downOverview(
            @RequestParam(required = false) String serviceName,
            @RequestParam(defaultValue = "24h") String range,
            @RequestParam(defaultValue = "5m") String interval
    ) {
        return ResponseEntity.ok(service.downOverview(serviceName, range, interval));
    }

    @GetMapping("/health/checks/latest")
    public ResponseEntity<List<ServiceStatus>> latest(
            @RequestParam(required = false) String serviceName
    ) {
        return ResponseEntity.ok(service.latestStatus(serviceName));
    }
}
