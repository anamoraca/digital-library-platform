package rs.ac.uns.acs.nais.GraphDatabaseService.mapper;

import org.mapstruct.Mapper;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.AuthorDto;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Author;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

    AuthorDto toDto(Author e);
    Author toEntity(AuthorDto d);
}
