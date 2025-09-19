package com.example.BookCatalogueApplication.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.BookCatalogueApplication.model.Book;
import com.example.BookCatalogueApplication.model.Collection;
import com.example.BookCatalogueApplication.model.Tag;
import com.example.BookCatalogueApplication.repository.BookRepo;
import com.example.BookCatalogueApplication.repository.CollectionRepo;
import com.example.BookCatalogueApplication.repository.TagRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookService {

    @Autowired
    BookRepo repo;

    @Autowired
    TagRepo tagRepo;

    @Autowired
    CollectionRepo collectionRepo;

    @Autowired
    Cloudinary cloudinary;

    public Page<Book> getBooks(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        return repo.findAll(pageable);
    }

    public Book getBookById(int id) {
        return repo.findById(id).orElse(null);
    }

    public Book getBookByIsbn(String isbn) {
        return repo.findByIsbn(isbn).orElse(null);
    }

    public Book addBook(Book book, MultipartFile image) throws IOException {
        if (repo.findByIsbn(book.getIsbn()).isPresent())
            throw new IllegalArgumentException("Book with this isbn already exists");

        Map uploadResult = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.asMap(
                "folder", "bookcovers"
        ));

        book.setImageName(uploadResult.get("public_id").toString());
        book.setImageUrl(uploadResult.get("secure_url").toString());
        
        if (book.getTags()!=null) {
            Set<Tag> tagSet = book.getTags().stream()
                    .map(tag -> tagRepo.findByName(tag.getName())
                            .orElseGet(() -> {
                                Tag newTag = new Tag();
                                newTag.setName(tag.getName());
                                return tagRepo.save(newTag);
                            })
                    )
                    .collect(Collectors.toSet());
            book.setTags(tagSet);
        }

        return repo.save(book);
    }

    public Book updateBook(int id, Book updatedBook, MultipartFile image) throws IOException {
        Book existingBook = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id " + id));

        if (image != null && !image.isEmpty()) {
            cloudinary.uploader().destroy(existingBook.getImageName(), ObjectUtils.emptyMap());
            Map uploadResult = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.asMap(
                    "folder", "bookcovers"
            ));
            existingBook.setImageName(uploadResult.get("public_id").toString());
            existingBook.setImageUrl(uploadResult.get("secure_url").toString());
        }

        existingBook.setTitle(updatedBook.getTitle());
        existingBook.setAuthor(updatedBook.getAuthor());
        existingBook.setIsbn(updatedBook.getIsbn());
        existingBook.setPublisher(updatedBook.getPublisher());
        existingBook.setPages(updatedBook.getPages());
        existingBook.setAvailable(updatedBook.isAvailable());
        existingBook.setPublishDate(updatedBook.getPublishDate());
        existingBook.setPublishPlace(updatedBook.getPublishPlace());

        Set<Tag> tagSet = updatedBook.getTags().stream()
                .map(tag -> tagRepo.findByName(tag.getName())
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tag.getName());
                            return tagRepo.save(newTag);
                        })
                )
                .collect(Collectors.toSet());
        existingBook.setTags(tagSet);
        return repo.save(existingBook);
    }

    public void deleteBook(int id) throws IOException {
        Book book = repo.findById(id).orElse(null);
        if (book != null) {
            cloudinary.uploader().destroy(book.getImageName(), ObjectUtils.emptyMap());
            book.getTags().clear();

            List<Collection> collections = collectionRepo.findAll();
            for (Collection collection : collections) {
                collection.getBooks().remove(book);
            }

            repo.delete(book);
        }
    }

    public Page<Book> searchBooks(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repo.searchBooks(keyword,pageable);
    }


}
