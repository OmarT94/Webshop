package java_work.de.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegistrationDTO(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        String role
) {
}
