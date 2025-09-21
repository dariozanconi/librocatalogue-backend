package com.example.BookCatalogueApplication.repository;

import com.example.BookCatalogueApplication.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollectionRepo extends JpaRepository<Collection, Integer> {

    Optional<Collection> findByName(String name);

    @Query("SELECT c FROM Collection c JOIN c.books b WHERE b.id = :bookId")
    List<Collection> findCollectionsByBookId(@Param("bookId") int id);

    @Query("SELECT DISTINCT c from Collection c " +
            "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Collection> searchCollections(String keyword);
}
