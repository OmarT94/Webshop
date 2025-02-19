package java_work.de.backend.model;


import java.util.List;

public record User(

        String email,
        String password,
        String firstName,
        String lastName,
        Role role, // ROLE_USER oder ROLE_ADMIN
        List<Address> addresses
)

{
    public enum Role {
        ROLE_ADMIN,
        ROLE_USER
    }

}
