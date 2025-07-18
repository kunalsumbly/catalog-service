package com.polarbookshop.catalogservice.domain.service;

import com.polarbookshop.catalogservice.cache.RedisService;
import com.polarbookshop.catalogservice.domain.exception.BookAlreadyExistsException;
import com.polarbookshop.catalogservice.domain.exception.BookNotFoundException;
import com.polarbookshop.catalogservice.domain.model.Book;
import com.polarbookshop.catalogservice.domain.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final RedisService redisService;

    public BookService (BookRepository bookRepository, RedisService redisService) {
        this.bookRepository = bookRepository;
        this.redisService = redisService;
    }

    public Iterable<Book> viewBookList() {
        // First check in Redis cache
        List<Book> cachedBooks = redisService.getAllBooks();
        if (cachedBooks != null) {
            log.info("Books found in cache");
            return cachedBooks;
        }
        log.info("Books not found in cache, fetching from database");
        
        // If not in cache, get from database
        Iterable<Book> books = bookRepository.findAll();
        
        // Convert Iterable to List for caching
        List<Book> bookList = new ArrayList<>();
        books.forEach(bookList::add);
        
        // Cache the books in Redis with TTL
        redisService.setAllBooks(bookList);
        
        return books;
    }

    public Book viewBookDetails(String isbn) {
        // First check in Redis cache
        Book cachedBook = redisService.getBook(isbn);
        if (cachedBook != null) {
            log.info("Book {} found in cache", isbn);
            return cachedBook;
        }
        log.info("Book {} not found in cache fetching from database", isbn);
        
        // If not in cache, get from database
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));
        
        // Cache the book in Redis with TTL (20 seconds)
        redisService.setBook(isbn, book);
        
        return book;
    }

    @Transactional
    public Book addBookToCatalog(Book book) {
        if (bookRepository.existsByIsbn(book.isbn())) {
            throw new BookAlreadyExistsException(book.isbn());
        }
        Book savedBook = bookRepository.save(book);
        log.info("Book {} added to catalog", savedBook.isbn());
        // Cache the new book in Redis
        redisService.setBook(savedBook.isbn(), savedBook);
        log.info("Book {} added to cache", savedBook.isbn());

        
//        // Invalidate the list cache since we've added a new book
        redisService.deleteAllBooks();
//
        return savedBook;
    }

    @Transactional
    public void removeBookFromCatalog(String isbn) {
        // Delete from database
        bookRepository.deleteByIsbn(isbn);
        log.info("Book {} removed from catalog", isbn);
        
        // Delete from Redis cache
        redisService.deleteBook(isbn);
        log.info("Book {} removed from cache", isbn);
        
//        // Invalidate the list cache since we've removed a book
        redisService.deleteAllBooks();
    }

    @Transactional
    public Book editBookDetails(String isbn, Book book) {
        return bookRepository.findByIsbn(isbn)
                .map(existingBook -> {
                    var bookToEdit = new Book(
                           existingBook.id(),
                           existingBook.isbn(),
                           book.title(),
                           book.author(),
                           book.price(),
                          existingBook.createdDate(),
                          existingBook.lastModifiedDate(),
                          existingBook.version()
                        );
                    Book updatedBook = bookRepository.save(bookToEdit);
                    log.info("Book {} updated in catalog", updatedBook.isbn());
                    // Update Redis cache
                    redisService.setBook(isbn, updatedBook);
                    log.info("Book {} updated in cache", updatedBook.isbn());
                    
//                    // Invalidate the list cache since we've updated a book
                    redisService.deleteAllBooks();
                    
                    return updatedBook;
                })
                .orElseGet(() -> addBookToCatalog(book));
    }

}
