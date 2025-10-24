package com.example.BookCatalogueApplication.model;

import java.time.LocalDate;
import java.util.Set;

public record BookPrivateDto(
        int id,
        String isbn,
        String title,
        String author,
        String authorSort,
        boolean available,
        LocalDate publishDate,
        String publishPlace,
        String publisher,
        int pages,
        String imageName,
        String imageUrl,
        String description, //get the description if authenticated
        Set<Tag> tags,
        LocalDate creationDate
) {}
