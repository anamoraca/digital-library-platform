package rs.ac.uns.acs.nais.BookSagaService.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import rs.ac.uns.acs.nais.BookSagaService.dto.BookDto;
import rs.ac.uns.acs.nais.BookSagaService.dto.CreateBookRequest;
import rs.ac.uns.acs.nais.BookSagaService.service.impl.SagaService;


@RestController
@RequestMapping("/api/saga/books")
@RequiredArgsConstructor
public class SagaBookController {

    private final SagaService saga;

    @GetMapping("/{id}")
    public Mono<BookDto> getById(@PathVariable String id) {
        return saga.getBook(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BookDto> create(@RequestBody @Valid CreateBookRequest req) {
        return saga.createBook(req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        return saga.deleteBook(id);
    }
}
