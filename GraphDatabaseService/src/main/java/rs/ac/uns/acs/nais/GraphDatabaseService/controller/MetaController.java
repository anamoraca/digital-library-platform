package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.AuthorDto;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.GenreDto;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.PublisherDto;
import rs.ac.uns.acs.nais.GraphDatabaseService.mapper.AuthorMapper;
import rs.ac.uns.acs.nais.GraphDatabaseService.mapper.GenreMapper;
import rs.ac.uns.acs.nais.GraphDatabaseService.mapper.PublisherMapper;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.AuthorRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.GenreRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.PublisherRepository;

import java.util.List;

@RestController
@RequestMapping("/api/graph/meta")
@RequiredArgsConstructor
public class MetaController {

    private final AuthorRepository authors;
    private final GenreRepository genres;
    private final PublisherRepository publishers;
    private final AuthorMapper authorMapper;
    private final GenreMapper genreMapper;
    private final PublisherMapper publisherMapper;

    @GetMapping("/authors")
    public List<AuthorDto> authors() {
        return authors.findAll().stream().map(authorMapper::toDto).toList();
    }

    @PostMapping("/authors")
    public AuthorDto createAuthor(@RequestBody @Valid AuthorDto d) {
        var e = authorMapper.toEntity(d);
        return authorMapper.toDto(authors.save(e));
    }

    @GetMapping("/genres")
    public List<GenreDto> genres() {
        return genres.findAll().stream().map(genreMapper::toDto).toList();
    }

    @PostMapping("/genres")
    public GenreDto createGenre(@RequestBody @Valid GenreDto d) {
        var e = genreMapper.toEntity(d);
        return genreMapper.toDto(genres.save(e));
    }

    @GetMapping("/publishers")
    public List<PublisherDto> publishers() {
        return publishers.findAll().stream().map(publisherMapper::toDto).toList();
    }

    @PostMapping("/publishers")
    public PublisherDto createPublisher(@RequestBody @Valid PublisherDto d) {
        var e = publisherMapper.toEntity(d);
        return publisherMapper.toDto(publishers.save(e));
    }
}

