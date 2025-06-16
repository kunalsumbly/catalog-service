package com.polarbookshop.catalogservice.web;

import com.polarbookshop.catalogservice.domain.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookJsonTests {

    @Autowired
    private JacksonTester<Book> json;

    @Test
    void testSerialize() throws Exception {
        var book = Book.build("1234567890", "Polar Bookshop", "A book about polar bears", 10.0);
        var jsonContent = json.write(book);
        assertThat(jsonContent).extractingJsonPathStringValue("@.isbn")
                                .isEqualTo(book.isbn());
        assertThat(jsonContent).extractingJsonPathStringValue("@.title")
                                .isEqualTo(book.title());
        assertThat(jsonContent).extractingJsonPathStringValue("@.author")
                                .isEqualTo(book.author());
        assertThat(jsonContent).extractingJsonPathNumberValue("@.price")
                                .isEqualTo(book.price());
    }

    @Test
    void testDeserialise() throws Exception {
        var content = "{\"isbn\":\"1234567890\",\"title\":\"Polar Bookshop\",\"author\":\"A book about polar bears\",\"price\":10.0}";
        var book = json.parse(content).getObject();
        assertThat(book.isbn()).isEqualTo("1234567890");
        assertThat(book.title()).isEqualTo("Polar Bookshop");
        assertThat(book.author()).isEqualTo("A book about polar bears");
        assertThat(book.price()).isEqualTo(10.0);
    }

}
