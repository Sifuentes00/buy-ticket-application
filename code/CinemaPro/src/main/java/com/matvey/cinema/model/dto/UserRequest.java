package com.matvey.cinema.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public class UserRequest {

    @NotBlank(message = "Поле 'username' не должно быть пустым")
    private String username;

    @NotBlank(message = "Поле 'email' не должно быть пустым")

    private String email;

    private List<Long> ticketIds; // Список идентификаторов билетов

    private List<Long> reviewIds; // Список идентификаторов отзывов

    // Конструкторы
    public UserRequest() {
    }

    public UserRequest(String username, String email, List<Long> ticketIds, List<Long> reviewIds) {
        this.username = username;
        this.email = email;
        this.ticketIds = ticketIds;
        this.reviewIds = reviewIds;
    }

    // Геттеры
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public List<Long> getTicketIds() {
        return ticketIds;
    }

    public List<Long> getReviewIds() {
        return reviewIds;
    }

    // Сеттеры
    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTicketIds(List<Long> ticketIds) {
        this.ticketIds = ticketIds;
    }

    public void setReviewIds(List<Long> reviewIds) {
        this.reviewIds = reviewIds;
    }
}
