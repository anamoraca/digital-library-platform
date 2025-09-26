package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.BookDto;
import rs.ac.uns.acs.nais.GraphDatabaseService.mapper.BookMapper;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final BookRepository books;
    private final AuthorRepository authors;
    private final GenreRepository genres;
    private final PublisherRepository publishers;
    private final BookMapper mapper;

    public Page<BookDto> list(int page, int size) {
        var p = PageRequest.of(page, size, Sort.by("title").ascending());
        return books.findAll(p).map(mapper::toDto);
    }

    public BookDto get(String id) {
        var e = books.findById(id).orElseThrow(() -> new NotFoundException("Book not found"));
        return mapper.toDto(e);
    }

    public BookDto create(BookDto d) {
        var e = mapper.partialToEntity(d);
        e.setAuthor(authors.findById(d.getAuthorId()).orElseThrow(() -> new NotFoundException("Author not found")));

        if (d.getGenreIds() != null) {
            List<Genre> gs = d.getGenreIds().stream()
                    .map(gid -> genres.findById(gid).orElseThrow(() -> new NotFoundException("Genre not found: " + gid)))
                    .toList();
            e.setGenres(gs);
        }

        if (d.getPublisherId() != null) {
            e.setPublisher(publishers.findById(d.getPublisherId())
                    .orElseThrow(() -> new NotFoundException("Publisher not found")));
        }

        return mapper.toDto(books.save(e));
    }

    public BookDto update(String id, BookDto d) {
        var e = books.findById(id).orElseThrow(() -> new NotFoundException("Book not found"));
        e.setTitle(d.getTitle());
        e.setYear(d.getYear());
        e.setLanguage(d.getLanguage());
        e.setPages(d.getPages());
        if (d.getAuthorId() != null) {
            e.setAuthor(authors.findById(d.getAuthorId()).orElseThrow(() -> new NotFoundException("Author not found")));
        }
        if (d.getGenreIds() != null) {
            var gs = d.getGenreIds().stream()
                    .map(gid -> genres.findById(gid).orElseThrow(() -> new NotFoundException("Genre not found: " + gid)))
                    .toList();
            e.setGenres(gs);
        }
        if (d.getPublisherId() != null) {
            e.setPublisher(publishers.findById(d.getPublisherId())
                    .orElseThrow(() -> new NotFoundException("Publisher not found")));
        }
        return mapper.toDto(books.save(e));
    }

    public void delete(String id) { books.deleteById(id); }
}
