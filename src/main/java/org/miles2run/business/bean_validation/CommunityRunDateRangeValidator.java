package org.miles2run.business.bean_validation;

import org.miles2run.business.domain.jpa.CommunityRun;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by shekhargulati on 01/08/14.
 */
public class CommunityRunDateRangeValidator implements ConstraintValidator<CommunityRunDateRange, CommunityRun> {

    @Override
    public void initialize(CommunityRunDateRange constraintAnnotation) {

    }

    @Override
    public boolean isValid(CommunityRun communityRun, ConstraintValidatorContext context) {
        if(communityRun == null){
            return true;
        }
        return communityRun.getStartDate().before(communityRun.getEndDate());
    }
}
