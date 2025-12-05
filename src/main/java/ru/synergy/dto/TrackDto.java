package ru.synergy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.synergy.model.Track;

/**
 * DTO for {@link Track}
 */
public record TrackDto (
        @NotBlank(message = "Название трека не может быть пустым")
        @Size(min = 1, max = 100, message = "Название трека должно быть от 1 до 100 символов")
        String title,

        @NotBlank(message = "Исполнитель не может быть пустым")
        @Size(min = 1, max = 100, message = "Имя исполнителя должно быть от 1 до 100 символов")
        String artist,

        String genre
){
}
