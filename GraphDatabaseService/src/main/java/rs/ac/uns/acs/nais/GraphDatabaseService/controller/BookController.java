package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.BookDto;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.impl.BookService;

@RestController
@RequestMapping("/api/graph/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    @GetMapping
    public Page<BookDto> list(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return service.list(page, size);
    }

    @GetMapping("/{id}")
    public BookDto get(@PathVariable String id) { return service.get(id); }

    @PostMapping
    public BookDto create(@RequestBody @Valid BookDto d) { return service.create(d); }

    @PutMapping("/{id}")
    public BookDto update(@PathVariable String id, @RequestBody @Valid BookDto d) { return service.update(id, d); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) { service.delete(id); }
}
