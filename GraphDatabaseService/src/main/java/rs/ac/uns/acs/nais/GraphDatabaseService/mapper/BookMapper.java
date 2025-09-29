package rs.ac.uns.acs.nais.GraphDatabaseService.mapper;

import org.mapstruct.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.BookDto;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Book;

@Mapper(componentModel = "spring", uses = {AuthorMapper.class, GenreMapper.class, PublisherMapper.class})
public interface BookMapper {

    @Mapping(target = "authorId", expression = "java(e.getAuthor()!=null ? e.getAuthor().getId() : null)")
    @Mapping(target = "genreIds", expression = "java(e.getGenres()!=null ? e.getGenres().stream().map(g -> g.getId()).toList() : null)")
    @Mapping(target = "publisherId", expression = "java(e.getPublisher()!=null ? e.getPublisher().getId() : null)")
    BookDto toDto(Book e);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "year", source = "year")
    @Mapping(target = "language", source = "language")
    @Mapping(target = "pages", source = "pages")
    Book partialToEntity(BookDto d);
}
