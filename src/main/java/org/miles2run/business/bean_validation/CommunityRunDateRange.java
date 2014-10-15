package org.miles2run.business.bean_validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by shekhargulati on 01/08/14.
 */
@Constraint(validatedBy = {CommunityRunDateRangeValidator.class})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommunityRunDateRange {

    String message() default "startDate should be before endDate";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
