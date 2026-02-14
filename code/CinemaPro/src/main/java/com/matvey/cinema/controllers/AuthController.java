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
@Tag(name = "Auth Controller", description = "API для аутентификации пользователя")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Авторизация пользователя",
            description = "Выполняет вход пользователя по нику и паролю (с проверкой bcrypt)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Вход выполнен успешно",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401",
                    description = "Неверные учетные данные", content = @Content)
    })
    public ResponseEntity<User> login(@Valid @RequestBody LoginRequestDto loginRequest) {

        logger.debug("Попытка входа для пользователя: {}", loginRequest.getUsername());

        Optional<User> userOptional = userService.authenticate(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        if (userOptional.isEmpty()) {
            logger.warn("Неверные учетные данные для пользователя: {}",
                    loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userOptional.get();
        logger.info("Пользователь {} успешно вошел.", user.getUsername());

        return ResponseEntity.ok(user);
    }

}