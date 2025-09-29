package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BookDto {
    private String id;

    @NotBlank
    private String title;

    private Integer year;

    private String language;

    private Integer pages;

    @NotBlank
    private String authorId;

    private List<String> genreIds;

    private String publisherId;
}
