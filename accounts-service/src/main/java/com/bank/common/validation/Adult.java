package com.bank.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AdultValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Adult {
    String message() default "Возраст должен быть не младше 18 лет";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
