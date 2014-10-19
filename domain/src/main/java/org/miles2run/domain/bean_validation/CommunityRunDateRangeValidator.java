package org.miles2run.domain.bean_validation;

import org.miles2run.domain.entities.CommunityRun;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CommunityRunDateRangeValidator implements ConstraintValidator<CommunityRunDateRange, CommunityRun> {

    @Override
    public void initialize(CommunityRunDateRange constraintAnnotation) {

    }

    @Override
    public boolean isValid(CommunityRun communityRun, ConstraintValidatorContext context) {
        if (communityRun == null) {
            return true;
        }
        return communityRun.getStartDate().before(communityRun.getEndDate());
    }
}
