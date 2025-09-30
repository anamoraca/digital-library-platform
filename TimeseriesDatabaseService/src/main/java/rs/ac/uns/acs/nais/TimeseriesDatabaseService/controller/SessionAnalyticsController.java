package rs.ac.uns.acs.nais.TimeseriesDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.AppSessionEvent;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.histogram.HistogramResponse;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.ActiveUsersDto;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.service.SessionAnalyticsService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/analytics/sessions")
@RequiredArgsConstructor
@Validated
public class SessionAnalyticsController {

    private final SessionAnalyticsService service;

    @PostMapping
    public ResponseEntity<Void> insert(@RequestBody AppSessionEvent event) {
        service.insert(event);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/batch")
    public ResponseEntity<Void> insertBatch(@RequestBody List<AppSessionEvent> events) {
        service.insertBatch(events);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteRange(
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String device,
            @RequestParam(required = false) String appVersion
    ) {
        service.deleteRange(from, to, userId, device, appVersion);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active-users")
    public ResponseEntity<ActiveUsersDto> activeUsers(
            @RequestParam(defaultValue = "15m") String window
    ) {
        return ResponseEntity.ok(service.activeUsers(window));
    }

    @GetMapping("/duration/histogram")
    public ResponseEntity<HistogramResponse> durationHistogram(
            @RequestParam(defaultValue = "7d") String range,
            @RequestParam(defaultValue = "10") int buckets,
            @RequestParam(defaultValue = "3600") int max
    ) {
        return ResponseEntity.ok(service.durationHistogram(range, buckets, max));
    }

}
