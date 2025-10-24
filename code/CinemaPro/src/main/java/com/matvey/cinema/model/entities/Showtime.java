package com.matvey.cinema.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "showtimes")
public class Showtime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dateTime;
    private String type;

    // <-- Связь с Фильмом (как мы добавляли) -->
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    //@JsonIgnore
    private Movie movie; // Предполагает наличие сущности Movie

    // <-- ДОБАВЛЕНО: Связь с Театром -->
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false) // Указывает на колонку theater_id в БД
    @JsonIgnore
    private Theater theater; // Предполагает наличие сущности Theater
    // <-- КОНЕЦ ДОБАВЛЕНО -->


    @OneToMany(mappedBy = "showtime", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Ticket> tickets = new ArrayList<>();

    public Showtime() {
    }

    // Возможно, обновите конструктор, если используете
    // public Showtime(String dateTime, String type, Movie movie, Theater theater) { ... }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // <-- Геттер и Сеттер для Movie -->
    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }
    // <-- КОНЕЦ -->

    // <-- ДОБАВЛЕНО: Геттер и Сеттер для Theater -->
    public Theater getTheater() {
        return theater;
    }

    public void setTheater(Theater theater) {
        this.theater = theater;
    }
    // <-- КОНЕЦ ДОБАВЛЕНО -->


    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }
}