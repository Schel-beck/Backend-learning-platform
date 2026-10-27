package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserDTO extends dk.bugelhartmann.UserDTO {
    private String username;
    private String password;
    private Set<String> roles = new HashSet<>(Set.of("ANYONE"));

    public UserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public UserDTO(String username, Set<String> roles) {
        this.username = username;
        this.roles = roles;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.username, this.roles});
    }

}
