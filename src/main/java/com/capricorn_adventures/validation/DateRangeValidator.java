package com.capricorn_adventures.validation;

import com.capricorn_adventures.dto.RoomSearchRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, RoomSearchRequest> {

    @Override
    public boolean isValid(RoomSearchRequest request, ConstraintValidatorContext context) {
        // If either date is null, let @NotNull handle it
        if (request.getCheckIn() == null || request.getCheckOut() == null) {
            return true;
        }

        boolean isValid = request.getCheckOut().isAfter(request.getCheckIn());

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Check-out date must be strictly after check-in date"
            ).addPropertyNode("checkOut").addConstraintViolation();
        }

        return isValid;
    }
}
