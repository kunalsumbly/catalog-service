//package com.polarbookshop.catalogservice.domain.repository;
//
//import com.polarbookshop.catalogservice.domain.model.Book;
//import org.springframework.stereotype.Repository;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Repository
//public class InMemoryBookRepository implements BookRepository {
//
//    private static final Map<String, Book> books = new ConcurrentHashMap();
//
//    @Override
//    public Iterable<Book> findAll() {
//        return books.values();
//    }
//
//    @Override
//    public Optional<Book> findByIsbn(String isbn) {
//        return existsByIsbn(isbn) ? Optional.of(books.get(isbn)) :
//                Optional.empty();
//    }
//
//    @Override
//    public boolean existsByIsbn(String isbn) {
//        return books.containsKey(isbn);
//    }
//
//    @Override
//    public Book save(Book book) {
//        books.put(book.isbn(), book);
//        return book;
//    }
//
//    @Override
//    public void deleteByIsbn(String isbn) {
//        books.remove(isbn);
//    }
//}
