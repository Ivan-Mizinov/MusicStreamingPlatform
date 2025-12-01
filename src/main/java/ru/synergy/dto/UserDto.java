package ru.synergy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.synergy.model.User;

/**
 * DTO for {@link User}
 */
public record UserDto(
        @Size(message = "Логин должен быть от 8 до 20 символов", min = 8, max = 20) @NotBlank(message = "Логин не может быть пустым") String username,
        @Size(message = "Пароль должен быть от 8 до 20 символов", min = 8, max = 20) @NotBlank(message = "Пароль не может быть пустым") String password) {
}