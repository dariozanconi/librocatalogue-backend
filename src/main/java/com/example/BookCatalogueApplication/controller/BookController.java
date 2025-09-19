package com.example.BookCatalogueApplication.controller;

import com.example.BookCatalogueApplication.model.Book;
import com.example.BookCatalogueApplication.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BookController {

    @Autowired
    BookService service;

    @GetMapping("/books")
    public ResponseEntity<Page<Book>> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy) {
        return new ResponseEntity<>(service.getBooks(page, size, sortBy), HttpStatus.OK);
    }

    @GetMapping("/books/id/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable int id) {
        Book book = service.getBookById(id);
        if (book!=null) {
            return new ResponseEntity<>(service.getBookById(id), HttpStatus.OK);
        } else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/books/isbn/{isbn}")
    public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) {
        Book book = service.getBookByIsbn(isbn);
        if (book!=null) {
            return new ResponseEntity<>(service.getBookByIsbn(isbn), HttpStatus.OK);
        } else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/books")
    public ResponseEntity<?> addBook(@RequestPart Book book,
                                     @RequestPart MultipartFile image) {
        try {
            Book savedBook = service.addBook(book, image);
            return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/books/id/{id}")
    public ResponseEntity<String> updateBook(@PathVariable int id,
                                             @RequestPart Book book,
                                             @RequestPart MultipartFile image) {
        try {
            Book book1 = service.updateBook(id, book, image);
            return new ResponseEntity<>("Book Updated", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @DeleteMapping("/books/id/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable int id) throws IOException {
        Book book = service.getBookById(id);
        if (book!=null) {
            service.deleteBook(id);
            return new ResponseEntity<>("Book deleted!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Book not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/books/search")
    public ResponseEntity<Page<Book>> searchBooksBKey(@RequestParam String keyword,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        Page<Book> books = service.searchBooks(keyword, page, size);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }


}
