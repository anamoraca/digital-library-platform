package rs.ac.uns.acs.nais.GraphDatabaseService.mapper;

import org.mapstruct.Mapper;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.UserDto;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User e);
    User toEntity(UserDto d);
}
