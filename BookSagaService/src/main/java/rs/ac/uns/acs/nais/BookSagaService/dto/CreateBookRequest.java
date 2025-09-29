package rs.ac.uns.acs.nais.BookSagaService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateBookRequest(
        @NotBlank String title,
        @NotNull Integer year,
        List<String> authors,
        List<String> genres
) {}
