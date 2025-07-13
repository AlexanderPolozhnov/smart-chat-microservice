package com.alexander.smartchat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Size(min = 3, max = 50, message = "Длина имени пользователя должна быть от 3 до 50")
    String username,

    @NotBlank(message = "Пароль не должен быть пустым")
    @Size(min = 8, max = 100, message = "Длина пароля должна составлять не менее 8 символов")
    String password,

    @NotBlank(message = "Электронная почта не должна быть пустой")
    @Email(message = "Электронная почта должна быть действительной")
    @Size(max = 100, message = "Длина письма должна быть не более 100")
    String email
) {
}
