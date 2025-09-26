package rs.ac.uns.acs.nais.GraphDatabaseService.mapper;

import org.mapstruct.Mapper;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.GenreDto;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Genre;

@Mapper(componentModel = "spring")
public interface GenreMapper {

    GenreDto toDto(Genre e);
    Genre toEntity(GenreDto d);
}
