package com.polarbookshop.catalogservice.domain;

import com.polarbookshop.catalogservice.domain.model.Book;
import com.polarbookshop.catalogservice.domain.repository.BookRepository;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService (BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Iterable<Book> viewBookList() {
        return bookRepository.findAll();
    }

    public Book findBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundExceptoin(isbn));
    }

    public Book addBookToCatalog(Book book) {
        if (bookRepository.existsByIsbn(book.isbn())) {
            throw new BookAlreadyExistsException(book.isbn());
        }
        return bookRepository.save(book);
    }

    public void removeBookFromCatalog(Book book) {
        bookRepository.deleteByIsbn(book.isbn());
    }

    public Book editBookDetails(String isbn, Book book) {
        return bookRepository.findByIsbn(isbn)
                .map(existingBook -> {
                    var bookToEdit = new Book(
                           existingBook.isbn(),
                           book.title(),
                           book.author(),
                            book.price());
                    return bookRepository.save(bookToEdit);
                })
                .orElseGet(() -> addBookToCatalog(book));
    }

}
