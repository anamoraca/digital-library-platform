package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserDto {

    private String id;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    private List<String> preferences;
    private Instant registeredAt;
}
