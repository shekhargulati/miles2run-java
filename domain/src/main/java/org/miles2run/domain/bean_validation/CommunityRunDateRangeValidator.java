package org.miles2run.domain.bean_validation;

import org.miles2run.domain.entities.CommunityRun;
import org.miles2run.domain.entities.Duration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CommunityRunDateRangeValidator implements ConstraintValidator<DateRangeCheck, CommunityRun> {

    @Override
    public void initialize(DateRangeCheck constraintAnnotation) {

    }

    @Override
    public boolean isValid(CommunityRun communityRun, ConstraintValidatorContext context) {
        if (communityRun == null) {
            return true;
        }
        Duration duration = communityRun.getDuration();
        return duration.getStartDate().before(duration.getEndDate());
    }
}
