package com.matvey.cinema.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ShowtimeDateValidator implements ConstraintValidator<ValidShowtimeDate, String> {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm");

    @Override
    public void initialize(ValidShowtimeDate constraintAnnotation) {
        // This method is not implemented because there are no initialization
        // requirements for the ValidShowtimeDate annotation. If you attempt to
        // use this method, it will throw an exception to indicate that it is
        // not supported.
        throw new
            UnsupportedOperationException("Initialization is not supported for ValidShowtimeDate.");
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(value, FORMATTER);
            return !dateTime.isBefore(LocalDateTime.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
