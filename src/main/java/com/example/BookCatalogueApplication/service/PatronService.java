package com.example.BookCatalogueApplication.service;

import com.example.BookCatalogueApplication.model.Patron;
import com.example.BookCatalogueApplication.repository.PatronRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PatronService {

    @Autowired
    PatronRepo repo;

    public List<Patron> getAllPatrons() {
        return repo.findAll();
    }

    public Patron getPatronById(int id) {
        return repo.findById(id).orElse(null);
    }

    public Patron getPatronByBookId(int id) {
        return repo.findPatronByBookId(id);
    }

}
