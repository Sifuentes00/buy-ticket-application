package com.matvey.cinema.repository;

import com.matvey.cinema.model.entities.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    // <-- ДОБАВЛЕНО: Метод для поиска пользователя по нику И паролю -->
    Optional<User> findByUsernameAndPassword(String username, String password);


}