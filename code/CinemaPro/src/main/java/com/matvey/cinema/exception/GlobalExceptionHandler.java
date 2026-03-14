package com.matvey.cinema.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
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

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<String>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        logger.warn(
                "event=api_error type=validation_error status=400 path={} errors={}",
                request.getRequestURI(),
                errors,
                ex
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<List<String>> handleValidationException(
            ValidationException ex,
            HttpServletRequest request) {

        logger.warn(
                "event=api_error type=validation_error status=400 path={} errors={}",
                request.getRequestURI(),
                ex.getErrors(),
                ex
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getErrors());
    }

    @ExceptionHandler(CustomNotFoundException.class)
    public ResponseEntity<String> handleCustomNotFoundException(
            CustomNotFoundException ex,
            HttpServletRequest request) {

        logger.warn(
                "event=api_error type=not_found status=404 path={} message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        logger.warn(
                "event=api_error type=response_status_exception status={} path={} message={}",
                ex.getStatusCode().value(),
                request.getRequestURI(),
                ex.getReason(),
                ex
        );

        return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ResponseEntity<String> handleUnrecognizedPropertyException(
            UnrecognizedPropertyException ex,
            HttpServletRequest request) {

        logger.warn(
                "event=api_error type=invalid_json status=400 path={} message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        String errorMessage =
                "Ошибка: запрос содержит недопустимые поля. Проверьте тело запроса.";

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        logger.warn(
                "event=api_error type=method_not_allowed status=405 path={} method={}",
                request.getRequestURI(),
                ex.getMethod(),
                ex
        );

        String errorMessage =
                "Ошибка: Метод " + ex.getMethod() + " не поддерживается для данного URL.";

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorMessage);
    }

    @ExceptionHandler(FileNotReadyException.class)
    public ResponseEntity<String> handleFileNotReadyException(
            FileNotReadyException ex,
            HttpServletRequest request) {

        logger.warn(
                "event=api_error type=file_not_ready status=409 path={} message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTaskNotFoundException(
            TaskNotFoundException ex,
            HttpServletRequest request) {

        logger.warn(
                "event=api_error type=task_not_found status=404 path={} message={}",
                request.getRequestURI(),
                ex.getMessage()
        );

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(LogFileAccessException.class)
    public ResponseEntity<String> handleLogFileAccessException(
            LogFileAccessException ex,
            HttpServletRequest request) {

        logger.error(
                "event=api_error type=log_file_access status=404 path={} message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}