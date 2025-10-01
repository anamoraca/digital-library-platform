package rs.ac.uns.acs.nais.TimeseriesDatabaseService.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.common.TimeSeriesPoint;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.enums.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.ErrorTypeCount;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.service.ErrorAnalyticsService;

import java.time.Instant;   // <--- dodaj
import java.util.List;

@RestController
@RequestMapping("/api/analytics/errors")
@RequiredArgsConstructor
@Validated
public class ErrorAnalyticsController {

    private final ErrorAnalyticsService service;

    @PostMapping
    public ResponseEntity<Void> insert(@RequestBody ErrorLogEvent event) {
        service.insert(event);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/batch")
    public ResponseEntity<Void> insertBatch(@RequestBody List<ErrorLogEvent> events) {
        service.insertBatch(events);
        return ResponseEntity.accepted().build();
    }

    /**
     * Brisanje error slogova po vremenskom opsegu i opcionalnim filterima.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteRange(
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(required = false, name = "serviceName") String serviceName,
            @RequestParam(required = false) String errorType
    ) {
        service.deleteRange(from, to, serviceName, errorType);
        return ResponseEntity.noContent().build();
    }


    //todo: provjeriti parametre
    @GetMapping("/overview")
    public ResponseEntity<List<TimeSeriesPoint<Long>>> overview(
            @RequestParam(required = false, name = "serviceName") String serviceName,
            @RequestParam(defaultValue = "24h") String range,
            @RequestParam(defaultValue = "5m") String interval
    ) {
        return ResponseEntity.ok(service.overview(serviceName, range, interval));
    }

    @GetMapping("/by-type")
    public ResponseEntity<List<ErrorTypeCount>> byType(
            @RequestParam(required = false, name = "serviceName") String serviceName,
            @RequestParam(defaultValue = "24h") String range,
            @RequestParam(defaultValue = "10") @Positive int limit
    ) {
        return ResponseEntity.ok(service.byType(serviceName, range, limit));
    }




    @GetMapping("/search")
    public ResponseEntity<List<ErrorLogEvent>> search(
            @RequestParam(required = false, name = "serviceName") String serviceName,
            @RequestParam(required = false) String errorType,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "100") @Positive int limit
    ) {
        return ResponseEntity.ok(service.search(serviceName, errorType, from, to, limit));
    }


}
