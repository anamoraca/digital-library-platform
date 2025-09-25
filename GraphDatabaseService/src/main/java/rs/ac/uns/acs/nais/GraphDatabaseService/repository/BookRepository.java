package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.BookTrendDto;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.RecScoreDto;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.TopGenreDto;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Book;

import java.util.List;

public interface BookRepository extends Neo4jRepository<Book, String> {
    Page<Book> findAll(Pageable p);

    // 1 Top zanrovi korisnika i preporuke iz tih zanr
    @Query("""
    MATCH (u:User {id:$uid})-[:READ|:RATED]->(:Book)-[:HAS_GENRE]->(g:Genre)
    WITH u, g, count(*) AS gscore
    MATCH (rec:Book)-[:HAS_GENRE]->(g)
    WHERE NOT (u)-[:READ|:RATED]->(rec)
    WITH rec, sum(gscore) AS total
    RETURN rec ORDER BY total DESC LIMIT $limit
    """)
    List<Book> recByFavGenres(@Param("uid") String userId, @Param("limit") long limit);

    // 2
    @Query("""
    MATCH (u:User {id:$uid})-[:READ|:RATED]->(b:Book)<-[:READ|:RATED]-(other:User)
    WHERE other.id <> u.id
    WITH u, other, count(DISTINCT b) AS common
    WHERE common >= 2
    MATCH (other)-[:READ|:RATED]->(rec:Book)
    WHERE NOT (u)-[:READ|:RATED]->(rec)
    WITH rec, count(*) AS score
    RETURN rec ORDER BY score DESC LIMIT $limit
    """)
    List<Book> recBySimilarUsers(@Param("uid") String userId, @Param("limit") long limit);

    // 3 Autori koje korisnik visoko ocenjuje , nove knjige tih autora
    @Query("""
    MATCH (u:User {id:$uid})-[r:RATED]->(b:Book)-[:WRITTEN_BY]->(a:Author)
    WITH u, a, avg(r.stars) AS aavg
    WHERE aavg >= 4
    MATCH (a)<-[:WRITTEN_BY]-(rec:Book)
    WHERE NOT (u)-[:READ|:RATED]->(rec)
    RETURN rec ORDER BY aavg DESC LIMIT $limit
    """)
    List<Book> recByLikedAuthors(@Param("uid") String userId, @Param("limit") long limit);

    // 4 Slicno po zanru,  previse popularne
    @Query("""
    MATCH (u:User {id:$uid})-[:READ|:RATED]->(:Book)-[:HAS_GENRE]->(g:Genre)
    WITH u, collect(DISTINCT g) AS gs
    MATCH (rec:Book)-[:HAS_GENRE]->(g2)
    WHERE g2 IN gs AND NOT (u)-[:READ|:RATED]->(rec)
    OPTIONAL MATCH (:User)-[:READ]->(rec)
    WITH rec, count(*) AS reads
    WITH rec, (1.0 / log10(reads + 2)) AS score
    RETURN rec ORDER BY score DESC LIMIT $limit
    """)
    List<Book> recByGenreWithPenalty(@Param("uid") String userId, @Param("limit") long limit);

    // 5 Trendovi po godini i jeziku za DTO
    @Query("""
    MATCH (:User)-[rd:READ]->(b:Book)
    WHERE rd.startedAt IS NOT NULL
    WITH b.year AS year, b.language AS lang, count(*) AS reads
    RETURN year AS year, lang AS lang, reads AS reads
    ORDER BY reads DESC LIMIT $limit
    """)
    List<BookTrendDto> trendsByYearAndLang(@Param("limit") long limit);

    // 6 Top zaanrovi po korisniku zaDTO
    @Query("""
    MATCH (u:User {id:$uid})-[:READ|:RATED]->(:Book)-[:HAS_GENRE]->(g:Genre)
    RETURN g.id AS genreId, g.name AS genreName, count(*) AS cnt
    ORDER BY cnt DESC LIMIT $limit
    """)
    List<TopGenreDto> topGenresForUser(@Param("uid") String userId, @Param("limit") long limit);

    // 7  rangiranje-zanr poklapanje + pro ocena autora
    @Query("""
    MATCH (u:User {id:$uid})-[:READ|:RATED]->(:Book)-[:HAS_GENRE]->(g:Genre)
    WITH u, collect(DISTINCT g.id) AS gids
    MATCH (rec:Book)-[:HAS_GENRE]->(g2:Genre)
    WHERE g2.id IN gids AND NOT (u)-[:READ|:RATED]->(rec)
    OPTIONAL MATCH (rec)-[:WRITTEN_BY]->(a:Author)<-[:WRITTEN_BY]-(b2:Book)<-[r2:RATED]-(:User)
    WITH rec, avg(r2.stars) AS aAvg
    WITH rec, coalesce(aAvg, 3.0) AS authorScore
    WITH rec, (authorScore * 0.6) + (size([(rec)-[:HAS_GENRE]->(gx:Genre) WHERE gx.id IN gids | 1]) * 0.4) AS score
    RETURN rec.id AS bookId, rec.title AS title, score AS score
    ORDER BY score DESC LIMIT $limit
    """)
    List<RecScoreDto> hybridRecScore(@Param("uid") String userId, @Param("limit") long limit);

    // 8 Kompleksna CRUD operacija nad relacijom: ažuriraj progress READ veze
    @Query("""
    MATCH (u:User {id:$uid})-[rd:READ]->(b:Book {id:$bid})
    SET rd.progress = $progress
    RETURN rd
    """)
    Void updateReadProgress(@Param("uid") String userId, @Param("bid") String bookId, @Param("progress") Integer progress);

    // 9 Brisanje relacije WISHLISTED (kompleksniji DELETE na relaciji)
    @Query("""
    MATCH (u:User {id:$uid})-[w:WISHLISTED]->(b:Book {id:$bid})
    DELETE w
    """)
    Void removeWish(@Param("uid") String userId, @Param("bid") String bookId);
}
