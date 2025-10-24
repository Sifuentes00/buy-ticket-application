package com.matvey.cinema.controllers;

import com.matvey.cinema.model.dto.UserRequest;
import com.matvey.cinema.model.entities.Review;
import com.matvey.cinema.model.entities.Ticket;
import com.matvey.cinema.model.entities.User;
import com.matvey.cinema.service.ReviewService;
import com.matvey.cinema.service.TicketService;
import com.matvey.cinema.service.UserService;
import com.matvey.cinema.service.VisitCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "User Controller", description = "API для управления пользователями")
public class UserController {
    private final UserService userService;
    private final TicketService ticketService;
    private final ReviewService reviewService;
    private final VisitCounterService visitCounterService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, TicketService ticketService,
                          ReviewService reviewService, VisitCounterService visitCounterService) {
        this.userService = userService;
        this.ticketService = ticketService;
        this.reviewService = reviewService;
        this.visitCounterService = visitCounterService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID",
            description = "Возвращает пользователя с указанным ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404",
                    description = "Пользователь не найден", content = @Content)
    })
    public ResponseEntity<User> getUserById(
            @Parameter(description = "Идентификатор пользователя",
                    example = "1") @PathVariable Long id) {
        logger.debug("Запрос на получение пользователя с ID: {}", id);
        visitCounterService.writeVisit("/api/users/" + id);
        Optional<User> user = userService.findById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.error("Пользователь с ID {} не найден", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    @Operation(summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей в базе данных")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class)))
    })
    public ResponseEntity<List<User>> getAllUsers() {
        logger.debug("Запрос на получение всех пользователей");
        visitCounterService.writeVisit("/api/users");
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    // <-- ДОБАВЛЕНО: Конечная точка для получения билетов для конкретного пользователя -->
    @GetMapping("/{userId}/tickets") // Сопоставляется с GET /api/users/{userId}/tickets
    public ResponseEntity<List<Ticket>> getUserTickets(@PathVariable Long userId) {
        logger.info("Получен запрос на получение билетов для пользователя с ID: {}", userId);
        // Возможно, стоит добавить проверку, чтобы убедиться, что запрошенный userId соответствует ID аутентифицированного пользователя для безопасности
        List<Ticket> tickets = ticketService.findByUserId(userId); // <-- Вызвать метод в вашем TicketService
        logger.info("Возвращено {} билетов для пользователя с ID {}.", tickets.size(), userId);
        return ResponseEntity.ok(tickets);
    }

    @PostMapping
    @Operation(summary = "Создать нового пользователя",
            description = "Создает нового пользователя на основе предоставленных данных")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400",
                    description = "Неверные входные данные", content = @Content)
    })
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        // <-- ДОБАВЛЕНО ЛОГИРОВАНИЕ -->
        logger.debug("Received User object in UserController.createUser: {}", user);
        if (user != null) {
            logger.debug("Password field in received User object: {}", user.getPassword());
        }
        // <-- Конец ДОБАВЛЕННОГО ЛОГИРОВАНИЯ -->

        visitCounterService.writeVisit("/api/users");
        User createdUser = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/bulk")
    @Operation(summary = "Создать нескольких пользователей",
            description = "Создает нескольких пользователей на основе предоставленных данных")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователи успешно созданы",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400",
                    description = "Неверные входные данные", content = @Content)
    })
    public ResponseEntity<List<User>> createUsers(@Valid @RequestBody List<UserRequest>
                                                          userRequests) {
        logger.debug("Запрос на создание нескольких пользователей");
        List<User> createdUsers = userRequests.stream()
                .map(userRequest -> {
                    User user = new User();
                    user.setUsername(userRequest.getUsername());
                    user.setEmail(userRequest.getEmail());
                    // TODO: Здесь нужно установить пароль из UserRequest, если бы он там был
                    // user.setPassword(userRequest.getPassword());

                    if (userRequest.getTicketIds() != null) {
                        List<Ticket> tickets = userRequest.getTicketIds().stream()
                                .map(ticketId -> ticketService.findById(ticketId))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .toList();
                        user.setTickets(tickets);
                    }

                    if (userRequest.getReviewIds() != null) {
                        List<Review> reviews = userRequest.getReviewIds().stream()
                                .map(reviewId -> reviewService.findById(reviewId))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .toList();
                        user.setReviews(reviews);
                    }
                    // Здесь вызывается save, который без хеширования сохранит пароль как есть.
                    // Если бы в UserRequest был пароль и мы его здесь устанавливали, он бы сохранился.
                    return userService.save(user);
                })
                .toList();
        visitCounterService.writeVisit("/api/users/bulk");

        return ResponseEntity.status(HttpStatus.CREATED).body(createdUsers);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя",
            description = "Обновляет существующего пользователя с указанным ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400",
                    description = "Неверные входные данные", content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "Пользователь не найден", content = @Content)
    })
    public ResponseEntity<User> updateUser(
            @Parameter(description = "Идентификатор пользователя для обновления",
                    example = "1") @PathVariable Long id,
            @Valid @RequestBody User user) {
        logger.debug("Запрос на обновление пользователя с ID: {}", id);
        visitCounterService.writeVisit("/api/users/" + id);
        user.setId(id);
        // TODO: В реальном приложении здесь не следует позволять менять пароль напрямую
        // без отдельной процедуры или хеширования, если оно было.
        User updatedUser = userService.save(user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя",
            description = "Удаляет пользователя с указанным ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Пользователь успешно удален", content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "Пользователь не найден", content = @Content)
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "Идентификатор пользователя для удаления",
                    example = "1") @PathVariable Long id) {
        logger.debug("Запрос на удаление пользователя с ID: {}", id);
        userService.deleteById(id);
        visitCounterService.writeVisit("/api/users/" + id);
        return ResponseEntity.noContent().build();
    }
}