package com.matvey.cinema.model.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Номер места не должен быть пустым")
    private String seatNumber; // Номер места в формате "Ряд-Место", как на фронтенде

    @NotNull(message = "Цена не должна быть пустой")
    private BigDecimal price;

    @NotNull(message = "Сеанс не должен быть пустым")
    @ManyToOne
    @JoinColumn(name = "showtime_id", nullable = false)
    //@JsonBackReference
    private Showtime showtime;

    @NotNull(message = "Пользователь не должен быть пустым")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Место (сущность) не должно быть пустым")
    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false) // Внешний ключ к таблице seats
    private Seat seat; // <-- Relation to Seat entity

    public Ticket() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public void setShowtime(Showtime showtime) {
        this.showtime = showtime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }
}