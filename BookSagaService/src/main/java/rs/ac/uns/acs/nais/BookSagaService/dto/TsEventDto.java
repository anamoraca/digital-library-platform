package rs.ac.uns.acs.nais.BookSagaService.dto;

public record TsEventDto(
        String correlationId,
        String action,   // BOOK_CREATE, BOOK_DELETE
        String bookId
) {}
