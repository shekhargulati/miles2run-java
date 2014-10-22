package org.miles2run.domain.bean_validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = {CommunityRunDateRangeValidator.class})
@Retention(RetentionPolicy.RUNTIME)
public @interface DateRangeCheck {

    String message() default "startDate should be before endDate";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
