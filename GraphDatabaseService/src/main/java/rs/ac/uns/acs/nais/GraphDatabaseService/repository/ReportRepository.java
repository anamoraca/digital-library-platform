package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Book;

import java.util.List;
import java.util.Map;

public interface ReportRepository extends Neo4jRepository<Book, String> {
 // A) Knjige po zanru — vraxa liste STRINGOVA
 @Query("""
  MATCH (b:Book)
  OPTIONAL MATCH (b)-[:IN_GENRE]->(g:Genre)
  OPTIONAL MATCH (b)<-[:WROTE]-(a:Author)
  WITH b,
       [x IN collect(DISTINCT g.name) WHERE x IS NOT NULL] AS gcols,
       [x IN collect(DISTINCT a.name) WHERE x IS NOT NULL] AS acols
  WHERE ($genre IS NULL OR any(gg IN gcols WHERE toLower(gg) = toLower($genre)))
    AND ($yearFrom IS NULL OR b.year >= $yearFrom)
    AND ($yearTo   IS NULL OR b.year <= $yearTo)
  RETURN {
    title:   b.title,
    year:    b.year,
    authors: acols,
    genres:  gcols
  } AS row
  ORDER BY coalesce(b.year,0) DESC, b.title
  LIMIT $limit
  """)
 List<Map<String,Object>> listBooksByGenre(
         @Param("genre") String genre,
         @Param("yearFrom") Integer yearFrom,
         @Param("yearTo") Integer yearTo,
         @Param("limit") Long limit
 );

 // B) Aktivni korisnici
 @Query("""
  MATCH (u:User)-[r:READ]->(:Book)
  WITH u, count(r) AS reads
  WHERE reads >= coalesce($minReads,0)
  RETURN {id:u.id, name:u.name, reads:reads} AS row
  ORDER BY reads DESC, u.name
  LIMIT $limit
  """)
 List<Map<String,Object>> usersByReadCount(
         @Param("minReads") Long minReads,
         @Param("limit") Long limit
 );

 // C) Hibrid
 @Query("""
  MATCH (u:User {id:$uid})
  OPTIONAL MATCH (u)-[:READ]->(:Book)-[:IN_GENRE]->(g:Genre)
  WITH u, g.name AS gname, count(*) AS cnt
  ORDER BY cnt DESC
  WITH u, [x IN collect(gname) WHERE x IS NOT NULL][0..3] AS favGenres

  MATCH (b:Book)
  OPTIONAL MATCH (b)-[:IN_GENRE]->(g2:Genre)
  WITH u, favGenres, b,
       [x IN collect(DISTINCT g2.name) WHERE x IS NOT NULL] AS allGenNames
  WHERE (size(favGenres)=0 OR any(gg IN allGenNames WHERE gg IN favGenres))
    AND NOT (u)-[:READ|WISHLISTED]->(b)

  OPTIONAL MATCH (b)<-[:WROTE]-(a:Author)
  OPTIONAL MATCH (:User)-[r2:READ]->(b)
  OPTIONAL MATCH (:User)-[rt:RATED]->(b)

  WITH b.title AS title,
       [x IN collect(DISTINCT a.name) WHERE x IS NOT NULL] AS authors,
       allGenNames AS genres,
       count(DISTINCT r2) AS popularity,
       coalesce(avg(rt.score),0) AS avgRating

  RETURN { title:title, authors:authors, genres:genres, score: 2.0 + popularity + avgRating } AS row
  ORDER BY row.score DESC
  LIMIT $limit
  """)
 List<Map<String,Object>> complexHybrid(@Param("uid") String uid,
                                        @Param("limit") Long limit);
}