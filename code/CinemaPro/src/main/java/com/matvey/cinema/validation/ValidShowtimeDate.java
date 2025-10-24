package com.matvey.cinema.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ShowtimeDateValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface ValidShowtimeDate {

    String message() default
            "Дата должна иметь формат dd.MM.yyyy H:mm и быть не ранее текущей даты";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
