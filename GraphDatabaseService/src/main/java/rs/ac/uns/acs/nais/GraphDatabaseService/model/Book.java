package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.List;

@Node("Book")
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Book {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    private String title;

    private Integer year;
    private String language;
    private Integer pages;

    @Relationship(type = "WRITTEN_BY", direction = Relationship.Direction.OUTGOING)
    private Author author;

    @Relationship(type = "HAS_GENRE", direction = Relationship.Direction.OUTGOING)
    private List<Genre> genres;

    @Relationship(type = "PUBLISHED_BY", direction = Relationship.Direction.OUTGOING)
    private Publisher publisher;
}
