package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

@Node("Genre")
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Genre {

    @Id
    private String id;

    private String name;
}
