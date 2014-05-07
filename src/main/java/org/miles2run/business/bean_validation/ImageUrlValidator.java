package org.miles2run.business.bean_validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ImageUrlValidator implements ConstraintValidator<ImageUrl, CharSequence> {

    @Override
    public void initialize(ImageUrl constraintAnnotation) {

    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null || value.length() == 0) {
            return true;
        }
        String imageUrl = value.toString();
        imageUrl = imageUrl.trim().toLowerCase();
        return (imageUrl.endsWith(".jpg") || imageUrl.endsWith(".png") || imageUrl.endsWith(".jpeg") || imageUrl
                .endsWith(".gif")) ? true : false;
    }

}
