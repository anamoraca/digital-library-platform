package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RateRequest {
    private Integer stars; // 1..5
    private String comment;
}