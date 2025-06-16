package com.polarbookshop.catalogservice.domain.repository;

import com.polarbookshop.catalogservice.domain.model.Book;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends CrudRepository<Book, Long> {
//    Iterable<Book> findAll();

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

//    Book save(Book book);

    @Modifying
    @Transactional
    @Query("DELETE FROM book WHERE isbn = :isbn")
    void deleteByIsbn(String isbn);

}
