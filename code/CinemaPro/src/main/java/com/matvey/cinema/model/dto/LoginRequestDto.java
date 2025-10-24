package com.matvey.cinema.model.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDto {

    @NotBlank(message = "Поле 'username' не должно быть пустым")
    private String username;

    @NotBlank(message = "Поле 'password' не должно быть пустым")
    private String password;

    public LoginRequestDto() {
    }

    public LoginRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}