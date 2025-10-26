package com.example.BookCatalogueApplication.controller;

import com.example.BookCatalogueApplication.model.*;
import com.example.BookCatalogueApplication.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BookController {

    @Autowired
    BookService service;

    @GetMapping("/books")
    public ResponseEntity<Page<?>> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            Principal principal) {

        Page<Book> books = service.getBooks(page, size, sortBy);

        if (principal != null) {
            Page<BookPrivateDto> dtoPage = books.map(service::mapToPrivateDto);
            return ResponseEntity.ok(dtoPage);
        } else {
            Page<BookPublicDto> dtoPage = books.map(service::mapToPublicDto);
            return ResponseEntity.ok(dtoPage);
        }
    }

    @GetMapping("/books/id/{id}")
    public ResponseEntity<?> getBookById(@PathVariable int id, Principal principal) {
        Book book = service.getBookById(id);
        if (book!=null) {
            if (principal != null) {
                return ResponseEntity.ok(service.mapToPrivateDto(book));
            } else {
                return ResponseEntity.ok(service.mapToPublicDto(book));
            }
        } else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/books/isbn/{isbn}")
    public ResponseEntity<?> getBookByIsbn(@PathVariable String isbn, Principal principal) {
        Book book = service.getBookByIsbn(isbn);
        if (book!=null) {
            if (principal != null) {
                return ResponseEntity.ok(service.mapToPrivateDto(book));
            } else {
                return ResponseEntity.ok(service.mapToPublicDto(book));
            }
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
    public ResponseEntity<Page<?>> searchBooksBKey(@RequestParam String keyword,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size,
                                                      Principal principal) {
        Page<Book> books = service.searchBooks(keyword, page, size);
        if (principal != null) {
            Page<BookPrivateDto> dtoPage = books.map(service::mapToPrivateDto);
            return ResponseEntity.ok(dtoPage);
        } else {
            Page<BookPublicDto> dtoPage = books.map(service::mapToPublicDto);
            return ResponseEntity.ok(dtoPage);
        }
    }

    @PostMapping("/books/{bookId}/lend")
    public ResponseEntity<?> lendBookToPatron(@PathVariable int bookId,
                                              @RequestBody LendDto lendDto) {
        try {
            Patron saved = service.lendBookToPatron(bookId, lendDto);
            return ResponseEntity.ok(saved);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/books/{bookId}/return")
    public ResponseEntity<String> returnBook(@PathVariable int bookId) {
        try {
            service.returnBook(bookId);
            return ResponseEntity.ok("Book returned successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
