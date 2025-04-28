package com.polarbookshop.catalogservice.demo;

import com.polarbookshop.catalogservice.domain.model.Book;
import com.polarbookshop.catalogservice.domain.repository.BookRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * a way to use profile to setup test data
 */
@Component
@Profile("testData")
public class BookDataLoader {

    private final BookRepository bookRepository;
    public BookDataLoader(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadTestData() {
        var book1 = new Book("1234567890", "Polar Bookshop", "Lyra Silverstar", 10.0);
        var book2 = new Book("1234567891", "Siddhartha", "Herman Hesse", 12.0);
        bookRepository.save(book1);
        bookRepository.save(book2);
    }

}
