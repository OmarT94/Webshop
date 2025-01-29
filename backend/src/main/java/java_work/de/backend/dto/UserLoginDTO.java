package java_work.de.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginDTO(@NotBlank @Email String email,
                           @NotBlank String password) {
}
