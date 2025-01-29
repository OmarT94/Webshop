package java_work.de.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record UserPasswordUpdateDTO(
        @NotBlank String newPassword
) {
}
