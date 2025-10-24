package com.matvey.cinema.service.impl;

import com.matvey.cinema.cache.CacheKeys;
import com.matvey.cinema.cache.InMemoryCache;
import com.matvey.cinema.exception.CustomNotFoundException;
import com.matvey.cinema.model.entities.User;
import com.matvey.cinema.repository.UserRepository;
import com.matvey.cinema.service.UserService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final InMemoryCache cache;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, InMemoryCache cache) {
        this.userRepository = userRepository;
        this.cache = cache;
    }

    @Override
    public Optional<User> findById(Long id) {
        String cacheKey = CacheKeys.USER_PREFIX + id;
        logger.info("Поиск пользователя с ID: {}", id);

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Пользователь с ID: {} найден в кэше.", id);
            return Optional.of((User) cachedData.get());
        }

        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new CustomNotFoundException("Пользователь не найден с ID: " + id);
        }

        user.ifPresent(value -> {
            cache.put(cacheKey, value);
            logger.info("Пользователь с ID: {} добавлен в кэш.", id);
        });

        return user;
    }

    @Override
    public List<User> findAll() {
        String cacheKey = CacheKeys.USERS_ALL;
        logger.info("Получение всех пользователей.");

        Optional<Object> cachedData = cache.get(cacheKey);
        if (cachedData.isPresent()) {
            logger.info("Все пользователи найдены в кэше.");
            return (List<User>) cachedData.get();
        }

        List<User> users = userRepository.findAll();
        cache.put(cacheKey, users);
        logger.info("Все пользователи добавлены в кэш.");

        return users;
    }

    @Override
    public User save(User user) {
        // <-- ДОБАВЛЕНО ЛОГИРОВАНИЕ -->
        logger.debug("User object received in UserServiceImpl.save: {}", user);
        if (user != null) {
            logger.debug("Password field in User object before saving: {}", user.getPassword());
        }
        // <-- Конец ДОБАВЛЕННОГО ЛОГИРОВАНИЯ -->

        // Пароль сохраняется как есть (строка)
        User savedUser = userRepository.save(user);

        cache.evict(CacheKeys.USERS_ALL);
        cache.evict(CacheKeys.USER_PREFIX + savedUser.getId());
        logger.info("Пользователь с ID: {} успешно сохранен и кэш очищен.", savedUser.getId());

        return savedUser;
    }

    @Override
    public void deleteById(Long id) {
        logger.info("Удаление пользователя с ID: {}", id);
        cache.evict(CacheKeys.USERS_ALL);
        cache.evict(CacheKeys.USER_PREFIX + id);

        if (!userRepository.existsById(id)) {
            throw new CustomNotFoundException("Пользователь не найден с ID: " + id);
        }

        userRepository.deleteById(id);
        logger.info("Пользователь с ID: {} успешно удален и кэш очищен.", id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        logger.info("Поиск пользователя по нику: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByUsernameAndPassword(String username, String password) {
        logger.info("Поиск пользователя по нику: {} и паролю (без хеширования).", username);
        return userRepository.findByUsernameAndPassword(username, password);
    }
}