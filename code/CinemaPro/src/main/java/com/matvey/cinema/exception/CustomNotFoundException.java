package com.matvey.cinema.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomNotFoundException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(CustomNotFoundException.class);

    public CustomNotFoundException(String message) {
        super(message);
        logger.error("Исключение CustomNotFoundException: {}", message);
    }

    public CustomNotFoundException(String message, Throwable cause) {
        super(message, cause);
        logger.error("Исключение CustomNotFoundException: {}", message, cause);
    }
}
