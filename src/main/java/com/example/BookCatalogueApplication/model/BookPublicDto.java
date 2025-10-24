package com.example.BookCatalogueApplication.model;

import java.time.LocalDate;
import java.util.Set;

public record BookPublicDto(
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
        Set<Tag> tags,
        LocalDate creationDate
) {}