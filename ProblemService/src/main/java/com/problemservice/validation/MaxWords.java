package com.problemservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MaxWordsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxWords {
    String message() default "Text exceeds maximum word limit";
    int value() default 2000;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

