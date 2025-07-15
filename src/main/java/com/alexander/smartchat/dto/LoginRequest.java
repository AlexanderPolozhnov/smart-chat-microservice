package com.alexander.smartchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Size(min = 3, max = 50, message = "Длина имени пользователя должна быть от 3 до 50")
    String username,

    @NotBlank(message = "Пароль не должен быть пустым")
    @Size(min = 8, max = 100, message = "Длина пароля должна составлять не менее 8 символов")
    String password
) {
}


