package com.problemservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxWordsValidator implements ConstraintValidator<MaxWords, String> {
    
    private int maxWords;

    @Override
    public void initialize(MaxWords constraintAnnotation) {
        this.maxWords = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Let @NotBlank handle empty values
        }
        
        // Count words by splitting on whitespace
        String[] words = value.trim().split("\\s+");
        return words.length <= maxWords;
    }
}

