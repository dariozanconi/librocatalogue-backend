package com.example.BookCatalogueApplication.controller;

import com.example.BookCatalogueApplication.model.Book;
import com.example.BookCatalogueApplication.model.Collection;
import com.example.BookCatalogueApplication.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CollectionController {

    @Autowired
    CollectionService service;

    @GetMapping("/collections")
    public ResponseEntity<List<Collection>> getAllCollections() {
        return new ResponseEntity<>(service.getAllCollection(), HttpStatus.OK);
    }

    @GetMapping("/collections/{id}/books")
    public ResponseEntity<Page<Book>> getCollectionBooks(@PathVariable int id,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        Page<Book> books = service.getCollectionBooks(id, page, size);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @PostMapping("/collections")
    public ResponseEntity<?> addCollection(@RequestBody Collection collectionDto) {
        Collection collection = new Collection(collectionDto.getName());
        try {
            Collection saved = service.addCollection(collection);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        }
        catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/collections/{id}/books")
    public ResponseEntity<?> addBook(@RequestBody Book book, @PathVariable int id) {
        Book book1 = service.addBook(book, id);
        if (book1 != null) {
            return new ResponseEntity<>(book1, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Book already saved in the collection", HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping("/collections/{collectionId}/books/{bookId}")
    public ResponseEntity<String> removeBook(@PathVariable int collectionId, @PathVariable int bookId) {
        try {
            service.removeBook(collectionId, bookId);
            return new ResponseEntity<>("Book removed successfully", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/collections/id/{id}")
    public ResponseEntity<String> removeCollection(@PathVariable int id) {
        try {
            service.removeCollection(id);
            return new ResponseEntity<>("Collection removed successfully", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Collection not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/book/{id}/collections")
    public ResponseEntity<List<Collection>> getCollectionsByBookId(@PathVariable int id) {
        List<Collection> collections = service.findCollectionsByBookId(id);
        if (collections.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(collections, HttpStatus.OK);
    }

    @GetMapping("/collections/search")
    public ResponseEntity<List<Collection>> searchCollectionByName(@RequestParam String keyword) {
        List<Collection> collections = service.searchCollections(keyword);
        return new ResponseEntity<>(collections, HttpStatus.OK);
    }

}
