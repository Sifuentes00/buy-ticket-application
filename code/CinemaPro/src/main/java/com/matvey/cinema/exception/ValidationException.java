package com.matvey.cinema.exception;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(ValidationException.class);
    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super("Validation failed with errors: " + String.join(", ", errors));
        this.errors = errors;
        logger.error("Validation failed with errors: {}", errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
