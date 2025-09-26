package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthorDto {
    private String id;
    @NotBlank
    private String name;
    private String bio;
    private String country;
    private Integer birthYear;
}
