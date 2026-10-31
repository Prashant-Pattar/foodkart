package org.projects.foodkart.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenDeliveryIsValid_thenNoViolations() {
        Delivery delivery = Delivery.builder()
                .order(new Order())
                .deliveryPartnerName("John Doe")
                .deliveryPartnerPhone("9876543210")
                .status(DeliveryStatus.ASSIGNED)
                .build();

        Set<ConstraintViolation<Delivery>> violations = validator.validate(delivery);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenDeliveryPartnerNameIsBlank_thenViolations() {
        Delivery delivery = Delivery.builder()
                .order(new Order())
                .deliveryPartnerName("")
                .deliveryPartnerPhone("9876543210")
                .status(DeliveryStatus.ASSIGNED)
                .build();

        Set<ConstraintViolation<Delivery>> violations = validator.validate(delivery);
        assertFalse(violations.isEmpty());
    }

    @Test
    void whenDeliveryPartnerPhoneIsInvalid_thenViolations() {
        Delivery delivery = Delivery.builder()
                .order(new Order())
                .deliveryPartnerName("John Doe")
                .deliveryPartnerPhone("123")
                .status(DeliveryStatus.ASSIGNED)
                .build();

        Set<ConstraintViolation<Delivery>> violations = validator.validate(delivery);
        assertFalse(violations.isEmpty());
    }
}
