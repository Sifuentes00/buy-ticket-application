package com.matvey.cinema.controllers;

import com.matvey.cinema.model.dto.LoginRequestDto;
import com.matvey.cinema.model.entities.User;
import com.matvey.cinema.service.UserService; // Или AuthService
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Auth Controller", description = "API для аутентификации пользователя")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    public AuthController(UserService userService /*, PasswordEncoder passwordEncoder*/) {
        this.userService = userService;
        // this.passwordEncoder = passwordEncoder; // <-- УДАЛЯЕМ ИНИЦИАЛИЗАЦИЮ
    }

    @PostMapping("/login")
    @Operation(summary = "Авторизация пользователя",
            description = "Выполняет вход пользователя по нику и паролю (ВНИМАНИЕ: без хеширования!)") // <-- Предупреждение
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Вход выполнен успешно",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401",
                    description = "Неверные учетные данные", content = @Content)
    })
    public ResponseEntity<User> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        logger.debug("Попытка входа для пользователя: {}", loginRequest.getUsername());

        // <-- ДОБАВЛЕНО: Использование нового метода поиска по нику И паролю -->
        // ВНИМАНИЕ: Сравнение паролей происходит напрямую в методе репозитория/сервиса!
        Optional<User> userOptional = userService.findByUsernameAndPassword(
                loginRequest.getUsername(),
                loginRequest.getPassword() // <-- Передаем введенный пароль
        );
        // <-- Конец нового поиска -->


        if (userOptional.isEmpty()) {
            logger.warn("Попытка входа с неверными учетными данными для пользователя: {}", loginRequest.getUsername());
            // Пользователь не найден ИЛИ пароль неверный
            // Возвращаем 401 Unauthorized, чтобы не раскрывать, существует ли пользователь с таким ником.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 3. Аутентификация успешна
        User user = userOptional.get();
        logger.info("Пользователь {} успешно вошел (без хеширования пароля).", user.getUsername());
        return ResponseEntity.ok(user); // Возвращаем данные пользователя (без пароля из-за @JsonIgnore)
    }

    // TODO: Возможно, добавить эндпоинт для регистрации здесь (POST /api/auth/register)
    // и переместить логику создания пользователя из UserController сюда.
    // При регистрации здесь нужно будет просто сохранить User с пришедшим plain-text паролем.
}