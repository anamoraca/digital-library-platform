package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

@RelationshipProperties
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RatedRel {

    @Id
    @GeneratedValue
    private Long id;

    @TargetNode
    private Book book;

    private Integer stars; // 1..5
    private String comment;
}
