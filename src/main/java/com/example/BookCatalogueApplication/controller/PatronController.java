package com.example.BookCatalogueApplication.controller;

import com.example.BookCatalogueApplication.model.Patron;
import com.example.BookCatalogueApplication.service.PatronService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PatronController  {

    @Autowired
    PatronService service;

    @GetMapping("/patrons")
    public ResponseEntity<List<Patron>> getAllPatrons() {
        return new ResponseEntity<List<Patron>>(service.getAllPatrons(), HttpStatus.OK);
    }

    @GetMapping("/patron/id/{id}")
    public ResponseEntity<?> getPatronById(@PathVariable int id) {
        Patron patron = service.getPatronById(id);
        if (patron!= null)
            return new ResponseEntity<>(patron, HttpStatus.OK);
        else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/book/{id}/patron")
    public ResponseEntity<?> getPatronByBookId(@PathVariable int id) {
        Patron patron = service.getPatronByBookId(id);
        if (patron!=null)
            return new ResponseEntity<Patron>(patron, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
