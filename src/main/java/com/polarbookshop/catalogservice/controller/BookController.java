package com.polarbookshop.catalogservice.controller;

import com.polarbookshop.catalogservice.domain.model.Book;
import com.polarbookshop.catalogservice.domain.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/books")
public class BookController {
    private final BookService bookService;
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Iterable<Book> findAll() {
        return bookService.viewBookList();
    }

    @GetMapping("{isbn}")
    public Book findBookByISBN(@PathVariable String isbn) {
        return bookService.viewBookDetails(isbn);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book saveBook(@Valid @RequestBody Book book) {
        return bookService.addBookToCatalog(book);
    }

    @PutMapping("{isbn}")
    public Book updateBook(@PathVariable String isbn, @Valid @RequestBody Book book) {
        return bookService.editBookDetails(isbn, book);
    }

    @DeleteMapping("{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable String isbn) {
         bookService.removeBookFromCatalog(isbn);
    }
}
