package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Node("User")
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    private String name;

    private String email;

    @Builder.Default
    private List<String> preferences = new ArrayList<>(); // genre ids

    private Instant registeredAt;

    @Relationship(type = "READ", direction = Relationship.Direction.OUTGOING)
    private List<ReadRel> read;

    @Relationship(type = "RATED", direction = Relationship.Direction.OUTGOING)
    private List<RatedRel> rated;

    @Relationship(type = "WISHLISTED", direction = Relationship.Direction.OUTGOING)
    private List<WishRel> wishlist;
}
