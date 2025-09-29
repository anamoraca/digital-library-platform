package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("Publisher")
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Publisher {

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;


    private String name;

    private String country;
}
