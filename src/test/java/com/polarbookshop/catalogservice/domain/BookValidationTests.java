package com.polarbookshop.catalogservice.domain;

import com.polarbookshop.catalogservice.domain.model.Book;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Example of unit test
 */
public class BookValidationTests {

    private static Validator validator;

    @BeforeAll
    static void setUp(){
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    void whenAllFieldsAreValid_thenNoErrors() {
        var book = new Book("1234567890", "Polar Bookshop", "A book about polar bears", 10.0);
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assert violations.isEmpty();
    }

    @Test
    void whenISBNIsInvalid_thenErrors() {
        var book = new Book("as23458790", "Polar Bookshop", "A book about polar bears", 10.0);
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("The ISBN format must be valid");
    }
}
