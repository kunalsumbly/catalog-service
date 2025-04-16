package com.polarbookshop.catalogservice.web;

import com.polarbookshop.catalogservice.controller.BookController;
import com.polarbookshop.catalogservice.domain.exception.BookNotFoundException;
import com.polarbookshop.catalogservice.domain.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
public class BookControllerMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void whenGetBookNotExistingThenReturnNotFound404() throws Exception {
        var isbn = "1234567890";
        given(bookService.viewBookDetails(isbn))
                .willThrow(BookNotFoundException.class);
        mockMvc.perform(get("/api/books/"+isbn))
                .andExpect(status().isNotFound());
    }

}
