package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.time.Instant;

@RelationshipProperties
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WishRel {

    @Id @GeneratedValue
    private Long id;

    @TargetNode
    private Book book;

    private Instant addedAt;
    private String note;
}
