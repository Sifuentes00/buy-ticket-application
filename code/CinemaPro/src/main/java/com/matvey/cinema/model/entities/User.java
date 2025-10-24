// В файле com.matvey.cinema.model.entities.User.java

package com.matvey.cinema.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore; // Импорт для игнорирования полей при сериализации
import jakarta.persistence.CascadeType; // Импорт для каскадных операций
import jakarta.persistence.Entity; // Импорт для объявления класса сущностью JPA
import jakarta.persistence.GeneratedValue; // Импорт для стратегии генерации первичного ключа
import jakarta.persistence.GenerationType; // Импорт для типов генерации первичного ключа
import jakarta.persistence.Id; // Импорт для объявления первичного ключа
import jakarta.persistence.OneToMany; // Импорт для отношения "один ко многим"
import jakarta.persistence.Table; // Импорт для маппинга сущности на таблицу
import jakarta.validation.constraints.Email; // Импорт для валидации формата email
import jakarta.validation.constraints.NotBlank; // Импорт для валидации на непустую строку
import jakarta.validation.constraints.Size; // Импорт для валидации размера строки
import java.util.ArrayList; // Импорт для использования ArrayList
import java.util.List; // Импорт для использования List

@Entity // Объявляет класс как сущность JPA
@Table(name = "users") // Маппит сущность на таблицу с именем "users"
public class User {
    @Id // Объявляет поле как первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Указывает, что значение первичного ключа генерируется базой данных
    private Long id; // Поле для хранения уникального идентификатора пользователя

    @NotBlank(message = "Имя пользователя не должно быть пустым") // Валидация: поле не должно быть пустым после удаления пробелов по краям
    @Size(min = 3, max = 50, message = "Имя пользователя должно содержать от 3 до 50 символов") // Валидация: размер строки должен быть в указанных пределах
    private String username; // Поле для хранения имени пользователя

    @NotBlank(message = "Email не должен быть пустым") // Валидация: поле не должно быть пустым
    @Email(message = "Некорректный формат email") // Валидация: поле должно соответствовать формату email
    private String email; // Поле для хранения email пользователя

    @NotBlank(message = "Пароль не должен быть пустым") // Валидация: поле не должно быть пустым
    private String password; // Поле для хранения пароля пользователя (в идеале - зашифрованного)

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Игнорирует это поле при сериализации сущности User в JSON, чтобы избежать рекурсии
    private List<Ticket> tickets = new ArrayList<>(); // Список билетов, принадлежащих пользователю

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    public User() {
        // Конструктор по умолчанию, необходимый для JPA
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    // Вспомогательные методы для управления двунаправленными связями (опционально, но хорошая практика)

    /**
     * Добавляет билет к пользователю и устанавливает обратную ссылку в билете.
     * @param ticket Билет для добавления.
     */
    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
        ticket.setUser(this);
    }

    /**
     * Удаляет билет у пользователя и снимает обратную ссылку в билете.
     * @param ticket Билет для удаления.
     */
    public void removeTicket(Ticket ticket) {
        tickets.remove(ticket);
        ticket.setUser(null);
    }

    /**
     * Добавляет отзыв к пользователю и устанавливает обратную ссылку в отзыве.
     * @param review Отзыв для добавления.
     */
    public void addReview(Review review) {
        reviews.add(review);
        review.setUser(this);
    }

    /**
     * Удаляет отзыв у пользователя и снимает обратную ссылку в отзыве.
     * @param review Отзыв для удаления.
     */
    public void removeReview(Review review) {
        reviews.remove(review);
        review.setUser(null);
    }
}
