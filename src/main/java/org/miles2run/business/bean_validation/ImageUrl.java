package org.miles2run.business.bean_validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = { ImageUrlValidator.class })
@Retention(RetentionPolicy.RUNTIME)
public @interface ImageUrl {

    String message() default "ImageUrl should end with .png or .jpg or .jpeg";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
