package com.matvey.cinema.service;

import com.matvey.cinema.model.entities.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findById(Long id);

    List<User> findAll();

    User save(User user); // <-- ПАРОЛЬ БУДЕТ СОХРАНЯТЬСЯ КАК STRING

    void deleteById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> authenticate(String username, String rawPassword);
}