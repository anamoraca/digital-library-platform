package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends Neo4jRepository<User, String> {
    Optional<User> findByEmail(String email);
    Page<User> findAll(Pageable pageable);

    @Query("""
      MATCH (u:User)
      WHERE toLower(u.name) CONTAINS toLower($q)
      RETURN u ORDER BY u.name
    """)
    List<User> searchByName(String q);
}
