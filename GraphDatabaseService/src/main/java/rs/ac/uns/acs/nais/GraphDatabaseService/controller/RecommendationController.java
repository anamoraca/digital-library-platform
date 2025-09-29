package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Book;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.impl.RecommendationService;

import java.util.List;

@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService svc;

    @GetMapping("/users/{uid}/recs/by-genres")
    public List<Book> byGenres(@PathVariable String uid, @RequestParam(defaultValue = "20") long limit) {
        return svc.byFavGenres(uid, limit);
    }

    @GetMapping("/users/{uid}/recs/by-similar")
    public List<Book> bySimilar(@PathVariable String uid, @RequestParam(defaultValue = "20") long limit) {
        return svc.bySimilarUsers(uid, limit);
    }

    @GetMapping("/users/{uid}/recs/by-authors")
    public List<Book> byAuthors(@PathVariable String uid, @RequestParam(defaultValue = "20") long limit) {
        return svc.byLikedAuthors(uid, limit);
    }

    @GetMapping("/users/{uid}/recs/genre-penalty")
    public List<Book> genrePenalty(@PathVariable String uid, @RequestParam(defaultValue = "20") long limit) {
        return svc.byGenrePenalty(uid, limit);
    }

    // Analytics/DTO
    @GetMapping("/analytics/trends")
    public List<BookTrendDto> trends(@RequestParam(defaultValue = "20") long limit) {
        return svc.trends(limit);
    }

    @GetMapping("/users/{uid}/analytics/top-genres")
    public List<TopGenreDto> topGenres(@PathVariable String uid, @RequestParam(defaultValue = "5") long limit) {
        return svc.topGenres(uid, limit);
    }

    @GetMapping("/users/{uid}/recs/hybrid")
    public List<RecScoreDto> hybrid(@PathVariable String uid, @RequestParam(defaultValue = "20") long limit) {
        return svc.hybrid(uid, limit);
    }
}
