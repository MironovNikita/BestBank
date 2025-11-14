package com.bank.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Pattern(regexp = "^[78]\\d*$", message = "Телефон должен начинаться с 7 или 8 и содержать только цифры")
@ReportAsSingleViolation
public @interface Phone {
    String message() default "Телефон должен начинаться с 7 или 8 и содержать только цифры";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
