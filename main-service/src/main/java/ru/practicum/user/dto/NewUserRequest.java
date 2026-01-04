package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewUserRequest {
    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 250, message = "Длинна имени должна быть от 2 до 250 символов")
    private String name;
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат электронной почты")
    @Size(min = 6, max = 254, message = "Длинна email должна быть от 6 до 254 символов")
    private String email;
}