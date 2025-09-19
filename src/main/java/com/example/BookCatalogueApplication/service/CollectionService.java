package com.example.BookCatalogueApplication.service;

import com.example.BookCatalogueApplication.model.Book;
import com.example.BookCatalogueApplication.model.Collection;
import com.example.BookCatalogueApplication.repository.BookRepo;
import com.example.BookCatalogueApplication.repository.CollectionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;


@Service
public class CollectionService {

    @Autowired
    CollectionRepo collectionRepo;

    @Autowired
    BookRepo bookRepo;

    public List<Collection> getAllCollection() {
        return collectionRepo.findAll();
    }

    public Page<Book> getCollectionBooks(int id, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepo.findBooksByCollectionId(id, pageable);
    }

    public Collection addCollection(Collection collection) {
        collection.setId(null);
        if (collectionRepo.findByName(collection.getName()).isPresent())
            throw new IllegalArgumentException("Collection with this name already exists");
        else return collectionRepo.save(collection);
    }

    public Book addBook(Book book, int id) {
        Collection collection = collectionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
        Book managedBook = bookRepo.findByIsbn(book.getIsbn())
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        if (collection.getBooks().contains(managedBook)) {
            return null;
        }
        collection.getBooks().add(managedBook);
        collectionRepo.save(collection);
        return managedBook;
    }


    public void removeBook(int collectionId, int bookId) {
        Collection collection = collectionRepo.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
        Book managedBook = bookRepo.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        if (!collection.getBooks().contains(managedBook)) {
            throw new IllegalArgumentException("Book not found in the collection");
        } else {
            collection.getBooks().remove(managedBook);
            collectionRepo.save(collection);
        }
    }

    public void removeCollection(int id) {
        Collection collection = collectionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
        collection.getBooks().clear();

        collectionRepo.delete(collection);
    }
}
