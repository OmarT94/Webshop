package java_work.de.backend.model;


public record User(

        String email,
        String password,
        String firstName,
        String lastName,
        Role role // ROLE_USER oder ROLE_ADMIN
)

{
    public enum Role {
        ROLE_ADMIN,
        ROLE_USER
    }

}
