package rs.ac.uns.acs.nais.BookSagaService.dto;

import java.util.List;

public record BookDto(
        String id,
        String title,
        Integer year,
        List<String> authors,
        List<String> genres
) {}
