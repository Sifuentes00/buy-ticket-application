package com.matvey.cinema.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<String>> handleValidationExceptions(MethodArgumentNotValidException
                                                                               ex) {
        logger.error("Ошибка валидации: {}", ex.getMessage(), ex);
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<List<String>> handleValidationException(ValidationException ex) {
        logger.error("Исключение валидации: {}", ex.getErrors(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getErrors());
    }

    @ExceptionHandler(CustomNotFoundException.class)
    public ResponseEntity<String> handleCustomNotFoundException(CustomNotFoundException ex) {
        logger.error("Исключение ненахода: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        logger.error("Исключение статуса ответа: {}", ex.getReason(), ex);
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ResponseEntity<String> handleUnrecognizedPropertyException(UnrecognizedPropertyException
                                                                                  ex) {
        logger.error("Лишние поля в JSON запросе: {}", ex.getMessage(), ex);
        String errorMessage = "Ошибка: запрос содержит недопустимые поля. Проверьте тело запроса.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorMessage);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String>
        handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        logger.error("Метод не поддерживается: {}", ex.getMessage(), ex);
        String errorMessage =
                "Ошибка: Метод " + ex.getMethod() + " не поддерживается для данного URL.";
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorMessage);
    }

    @ExceptionHandler(FileNotReadyException.class)
    public ResponseEntity<String> handleFileNotReadyException(FileNotReadyException ex) {
        logger.error("Ошибка: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTaskNotFoundException(TaskNotFoundException
                                                                                       ex) {
        logger.warn("Task not found exception: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(LogFileAccessException.class)
    public ResponseEntity<String> handleLogFileAccessException(LogFileAccessException ex) {
        logger.error("Исключение доступа к лог-файлу: {}", ex.getMessage(), ex);
        // Возвращаем статус 404 Not Found, так как файл либо не существует, либо недоступен
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

}
