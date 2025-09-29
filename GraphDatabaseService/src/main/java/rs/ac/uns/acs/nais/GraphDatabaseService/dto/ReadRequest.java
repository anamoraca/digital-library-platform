package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReadRequest {

    private Instant startedAt;
    private Instant finishedAt;
    private Integer progress;
}
