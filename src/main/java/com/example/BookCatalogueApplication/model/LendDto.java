package com.example.BookCatalogueApplication.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LendDto {

    private Patron patron;
    private String description;
}


