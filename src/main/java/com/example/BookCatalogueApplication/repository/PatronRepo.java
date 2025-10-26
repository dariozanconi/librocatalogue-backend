package com.example.BookCatalogueApplication.repository;

import com.example.BookCatalogueApplication.model.Book;
import com.example.BookCatalogueApplication.model.Patron;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatronRepo extends JpaRepository<Patron, Integer> {

    @Query("SELECT p FROM Patron p JOIN p.books b WHERE b.id = :bookId")
    Patron findPatronByBookId(@Param("bookId") int id);

    Optional<Patron> findByFirstName(String firstName);

    Optional<Patron> findByEmail(String firstName);
}
