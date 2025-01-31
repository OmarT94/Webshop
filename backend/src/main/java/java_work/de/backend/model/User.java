package java_work.de.backend.model;

import org.springframework.data.annotation.Id;

public record User(

        String email,
        String password,
        Role role // ROLE_USER oder ROLE_ADMIN
)

{
    public enum Role {
        ROLE_ADMIN,
        ROLE_USER
    }

}
