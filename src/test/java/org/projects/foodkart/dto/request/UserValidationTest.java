package org.projects.foodkart.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projects.foodkart.entity.Gender;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenUserRegisterRequestIsValid_thenNoViolations() {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .name("John Doe")
                .gender(Gender.MALE)
                .phoneNumber("9876543210")
                .email("john@example.com")
                .pinCode("560001")
                .build();

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenEmailIsInvalid_thenViolations() {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .name("John Doe")
                .gender(Gender.MALE)
                .phoneNumber("9876543210")
                .email("invalid-email")
                .pinCode("560001")
                .build();

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
}
