package com.example.BookCatalogueApplication.repository;

import com.example.BookCatalogueApplication.model.Book;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepo extends JpaRepository<Book, Integer> {

    @Override
    Page<Book> findAll(Pageable pageable);

    @Query("SELECT b FROM Collection c JOIN c.books b WHERE c.id = :collectionId")
    Page<Book> findBooksByCollectionId(@Param("collectionId") int collectionId, Pageable pageable);

    Optional<Book> findByIsbn(String isbn);

    @Query("SELECT DISTINCT b from Book b " +
            "LEFT JOIN b.tags t " +
            "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.publisher) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchBooks(String keyword, Pageable pageable);
}
