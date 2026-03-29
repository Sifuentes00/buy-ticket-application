package com.matvey.cinema.model.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "favorites")
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_user_id")
    private String keycloakUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    public Favorite() {}

    public Favorite(String keycloakUserId, Movie movie) {
        this.keycloakUserId = keycloakUserId;
        this.movie = movie;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKeycloakUserId() { return keycloakUserId; }
    public void setKeycloakUserId(String keycloakUserId) { this.keycloakUserId = keycloakUserId; }
    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }
}