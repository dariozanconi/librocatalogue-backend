package com.example.BookCatalogueApplication.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.BookCatalogueApplication.model.*;
import com.example.BookCatalogueApplication.model.Collection;
import com.example.BookCatalogueApplication.repository.BookRepo;
import com.example.BookCatalogueApplication.repository.CollectionRepo;
import com.example.BookCatalogueApplication.repository.PatronRepo;
import com.example.BookCatalogueApplication.repository.TagRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
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

    @Autowired
    PatronRepo patronRepo;

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

        Map uploadResult = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.asMap(
                "folder", "bookcovers"
        ));

        book.setImageName(uploadResult.get("public_id").toString());
        book.setImageUrl(uploadResult.get("secure_url").toString());

        if (book.getAuthor() != null) book.setAuthorSort(toSortableAuthor(book.getAuthor()));
        book.setCreationDate(LocalDate.now());

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
        if (updatedBook.getAuthor() != null) existingBook.setAuthorSort(toSortableAuthor(updatedBook.getAuthor()));
        existingBook.setIsbn(updatedBook.getIsbn());
        existingBook.setPublisher(updatedBook.getPublisher());
        existingBook.setPages(updatedBook.getPages());
        existingBook.setAvailable(updatedBook.isAvailable());
        existingBook.setPublishDate(updatedBook.getPublishDate());
        existingBook.setPublishPlace(updatedBook.getPublishPlace());
        existingBook.setCreationDate(LocalDate.now());

        existingBook.setDescription(updatedBook.getDescription());

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

    public Patron lendBookToPatron(int bookId, LendDto lendDto) {
        Book book = repo.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        if (!book.isAvailable()) {
            throw new IllegalStateException("Book is already lent out.");
        }

        Patron patron = patronRepo.findByEmail(lendDto.getPatron().getEmail())
                .orElseGet(() -> {
                    Patron newPatron = lendDto.getPatron();
                    newPatron.setCreationDate(LocalDate.now());
                    book.setAvailable(false);
                    book.setDescription(lendDto.getDescription());
                    book.setLendDate(LocalDate.now());
                    System.out.println(book.getLendDate());
                    repo.save(book);
                    if (newPatron.getBooks() == null) {
                        newPatron.setBooks(new HashSet<>());
                    }
                    newPatron.getBooks().add(book);
                    return patronRepo.save(newPatron);
                });

        book.setAvailable(false);
        book.setDescription(lendDto.getDescription());
        book.setLendDate(LocalDate.now());
        System.out.println(book.getLendDate());
        repo.save(book);
        if (patron.getBooks() == null) {
            patron.setBooks(new HashSet<>());
        }
        patron.getBooks().add(book);
        patronRepo.save(patron);

        return patron;
    }

    public void returnBook(int bookId) {
        Book book = repo.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        Patron patron = patronRepo.findPatronByBookId(bookId);
        if (patron == null) {
            throw new IllegalStateException("This book is not lent out.");
        }

        patron.getBooks().remove(book);
        patronRepo.save(patron);

        book.setAvailable(true);
        book.setDescription(null);
        book.setLendDate(null);
        repo.save(book);

        if (patron.getBooks().isEmpty()) {
            patronRepo.delete(patron);
        }
    }

    private String toSortableAuthor(String author) {
        String[] parts = author.split(" ");
        if (parts.length > 1) {
            String lastName = parts[parts.length - 1];
            String firstNames = String.join(" ", Arrays.copyOf(parts, parts.length - 1));
            return lastName + ", " + firstNames;
        }
        return author;
    }

    public BookPublicDto mapToPublicDto(Book book) {
        return new BookPublicDto(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getAuthorSort(),
                book.isAvailable(),
                book.getPublishDate(),
                book.getPublishPlace(),
                book.getPublisher(),
                book.getPages(),
                book.getImageName(),
                book.getImageUrl(),
                Optional.ofNullable(book.getTags()).orElse(Set.of()),
                book.getCreationDate()
        );
    }

    public BookPrivateDto mapToPrivateDto(Book book) {
        return new BookPrivateDto(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getAuthorSort(),
                book.isAvailable(),
                book.getPublishDate(),
                book.getPublishPlace(),
                book.getPublisher(),
                book.getPages(),
                book.getImageName(),
                book.getImageUrl(),
                book.getDescription(),
                Optional.ofNullable(book.getTags()).orElse(Set.of()),
                book.getCreationDate(),
                book.getLendDate()
        );
    }
}
