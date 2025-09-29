package rs.ac.uns.acs.nais.TimeseriesDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.responses.SeedResult;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.service.SeedService;

@RestController
@RequestMapping("/api/analytics/seed")
@RequiredArgsConstructor
public class SeedController {

    private final SeedService seed;

    @PostMapping("/all")
    public ResponseEntity<SeedResult> seedAll(
            @RequestParam(defaultValue = "500") int bookPairs,
            @RequestParam(defaultValue = "500") int sessionPairs,
            @RequestParam(defaultValue = "1000") int errorCount,
            @RequestParam(defaultValue = "7") int daysBack
    ) {
        return ResponseEntity.ok(seed.seedAll(bookPairs, sessionPairs, errorCount, daysBack));
    }

    @PostMapping("/books")
    public ResponseEntity<Integer> seedBooks(
            @RequestParam(defaultValue = "500") int pairs,
            @RequestParam(defaultValue = "7") int daysBack
    ) {
        return ResponseEntity.ok(seed.seedBooks(pairs, daysBack, null));
    }

    @PostMapping("/sessions")
    public ResponseEntity<Integer> seedSessions(
            @RequestParam(defaultValue = "500") int pairs,
            @RequestParam(defaultValue = "7") int daysBack
    ) {
        return ResponseEntity.ok(seed.seedSessions(pairs, daysBack, null));
    }

    @PostMapping("/errors")
    public ResponseEntity<Integer> seedErrors(
            @RequestParam(defaultValue = "1000") int count,
            @RequestParam(defaultValue = "7") int daysBack
    ) {
        return ResponseEntity.ok(seed.seedErrors(count, daysBack, null));
    }
}
