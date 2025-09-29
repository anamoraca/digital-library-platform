package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("Author")
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Author {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;


    private String name;

    private String bio;
    private String country;
    private Integer birthYear;
}
