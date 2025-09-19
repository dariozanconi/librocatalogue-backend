package com.example.BookCatalogueApplication.repository;

import com.example.BookCatalogueApplication.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollectionRepo extends JpaRepository<Collection, Integer> {

    Optional<Collection> findByName(String name);
}
