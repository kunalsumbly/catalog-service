package com.polarbookshop.catalogservice.cache;

import com.polarbookshop.catalogservice.domain.model.Book;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, Book> bookRedisTemplate;
    private final RedisTemplate<String, List<Book>> bookListRedisTemplate;
    
    // TTL for Book cache entries (20 seconds)
    private static final Duration BOOK_CACHE_TTL = Duration.ofSeconds(20);

    public RedisService(
            StringRedisTemplate stringRedisTemplate, 
            RedisTemplate<String, Book> bookRedisTemplate,
            RedisTemplate<String, List<Book>> bookListRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.bookRedisTemplate = bookRedisTemplate;
        this.bookListRedisTemplate = bookListRedisTemplate;
    }

    // String operations
    public void setValue(String key, String value) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // If inside a transaction, register a callback to execute after transaction commits
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    stringRedisTemplate.opsForValue().set(key, value);
                }
            });
        } else {
            // If not in a transaction, execute immediately
            stringRedisTemplate.opsForValue().set(key, value);
        }
    }

    public String getValue(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void deleteKey(String key) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // If inside a transaction, register a callback to execute after transaction commits
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    stringRedisTemplate.delete(key);
                }
            });
        } else {
            // If not in a transaction, execute immediately
            stringRedisTemplate.delete(key);
        }
    }
    
    // Book operations
    public void setBook(String isbn, Book book) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // If inside a transaction, register a callback to execute after transaction commits
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    bookRedisTemplate.opsForValue().set(isbn, book, BOOK_CACHE_TTL);
                }
            });
        } else {
            // If not in a transaction, execute immediately
            bookRedisTemplate.opsForValue().set(isbn, book, BOOK_CACHE_TTL);
        }
    }
    
    public Book getBook(String isbn) {
        return bookRedisTemplate.opsForValue().get(isbn);
    }
    
    public void deleteBook(String isbn) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // If inside a transaction, register a callback to execute after transaction commits
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    bookRedisTemplate.delete(isbn);
                }
            });
        } else {
            // If not in a transaction, execute immediately
            bookRedisTemplate.delete(isbn);
        }
    }


    // All books operations
    private static final String ALL_BOOKS_KEY = "all-books";
    
    public List<Book> getAllBooks() {
        return bookListRedisTemplate.opsForValue().get(ALL_BOOKS_KEY);
    }
    
    public void setAllBooks(List<Book> books) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // If inside a transaction, register a callback to execute after transaction commits
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    bookListRedisTemplate.opsForValue().set(ALL_BOOKS_KEY, books, BOOK_CACHE_TTL);
                }
            });
        } else {
            // If not in a transaction, execute immediately
            bookListRedisTemplate.opsForValue().set(ALL_BOOKS_KEY, books, BOOK_CACHE_TTL);
        }
    }
    
    public void deleteAllBooks() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // If inside a transaction, register a callback to execute after transaction commits
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    bookListRedisTemplate.delete(ALL_BOOKS_KEY);
                }
            });
        } else {
            // If not in a transaction, execute immediately
            bookListRedisTemplate.delete(ALL_BOOKS_KEY);
        }
    }
}