package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Book;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.BookRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final BookRepository repo;

    public List<Book> byFavGenres(String uid, long limit) { return repo.recByFavGenres(uid, limit); }
    public List<Book> bySimilarUsers(String uid, long limit) { return repo.recBySimilarUsers(uid, limit); }
    public List<Book> byLikedAuthors(String uid, long limit) { return repo.recByLikedAuthors(uid, limit); }
    public List<Book> byGenrePenalty(String uid, long limit) { return repo.recByGenreWithPenalty(uid, limit); }

    public List<BookTrendDto> trends(long limit) { return repo.trendsByYearAndLang(limit); }
    public List<TopGenreDto> topGenres(String uid, long limit) { return repo.topGenresForUser(uid, limit); }
    public List<RecScoreDto> hybrid(String uid, long limit) { return repo.hybridRecScore(uid, limit); }
}
