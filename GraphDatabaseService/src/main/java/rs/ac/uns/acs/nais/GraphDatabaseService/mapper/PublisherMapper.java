package rs.ac.uns.acs.nais.GraphDatabaseService.mapper;

import org.mapstruct.Mapper;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.PublisherDto;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Publisher;

@Mapper(componentModel = "spring")
public interface PublisherMapper {

    PublisherDto toDto(Publisher e);
    Publisher toEntity(PublisherDto d);
}
