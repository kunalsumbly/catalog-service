package com.polarbookshop.catalogservice;

import com.polarbookshop.catalogservice.domain.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CatalogServiceApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void whenPostRequestThenBookCreated() {
        webTestClient.post()
                .uri("/api/books")
                .bodyValue(new Book("1234567890", "The Hobbit", "Jack Sparrow", 10.0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Book.class).value(book -> {
                   assertThat(book).isNotNull();
                   assertThat(book.isbn()).isEqualTo("1234567890");
                });
    }

}
