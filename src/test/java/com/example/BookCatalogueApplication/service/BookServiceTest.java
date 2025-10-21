package com.example.BookCatalogueApplication.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.example.BookCatalogueApplication.model.Book;
import com.example.BookCatalogueApplication.model.Tag;
import com.example.BookCatalogueApplication.repository.BookRepo;
import com.example.BookCatalogueApplication.repository.TagRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock private BookRepo bookRepo;
    @Mock private TagRepo tagRepo;
    @Mock private Cloudinary cloudinary;
    @Mock private MultipartFile image;

    @InjectMocks
    private BookService bookService;

    @Test
    void shouldReturnBookIfIdExists() {

        Book mockBook = new Book(1,
                "123456789",
                "Demian",
                "Herman Hesse",
                "Hesse, Herman",
                true,
                LocalDate.now(),
                "Frankfurt",
                "Verlag",
                32,
                " ",
                " ",
                null
        );
        when(bookRepo.findById(1)).thenReturn(Optional.of(mockBook));

        Book resultBook = bookService.getBookById(1);

        assertEquals("Demian", resultBook.getTitle());
        verify(bookRepo).findById(1);

    }

    @Test
    void shouldReturnNullIfBookNotFound() {

        when(bookRepo.findById(99)).thenReturn(Optional.empty());

        Book resultBook = bookService.getBookById(99);

        assertNull(resultBook);
        verify(bookRepo).findById(99);
    }

    @Test
    void shouldReturnBookIsbnIdExists() {

        Book mockBook = new Book(1,
                "1234567890",
                "Demian",
                "Herman Hesse",
                "Hesse, Herman",
                true,
                LocalDate.now(),
                "Frankfurt",
                "Verlag",
                32,
                " ",
                " ",
                null
        );
        when(bookRepo.findByIsbn("1234567890")).thenReturn(Optional.of(mockBook));

        Book resultBook = bookService.getBookByIsbn("1234567890");

        assertEquals("Demian", resultBook.getTitle());
        verify(bookRepo).findByIsbn("1234567890");

    }

    @Test
    void shouldReturnNullIfBookNotFoundByIsbn() {

        when(bookRepo.findByIsbn("1234567890")).thenReturn(Optional.empty());

        Book resultBook = bookService.getBookByIsbn("1234567890");

        assertNull(resultBook);
        verify(bookRepo).findByIsbn("1234567890");
    }

    @Test
    void shouldSaveNewBook() throws IOException {

        Book mockBook = new Book(1,
                "1234567890",
                "Demian",
                "Herman Hesse",
                "",
                true,
                LocalDate.now(),
                "Frankfurt",
                "Verlag",
                32,
                " ",
                " ",
                null
        );
        mockBook.setTags(Set.of(new Tag(1L, "Fantasy")));

        when(image.getBytes()).thenReturn("fake-image".getBytes());
        when(bookRepo.findByIsbn(anyString())).thenReturn(Optional.empty());

        Uploader uploaderMock = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploaderMock);

        Map<String, Object> uploadResult = Map.of(
                "public_id", "cover123",
                "secure_url", "https://cloudinary/cover123"
        );

        when(uploaderMock.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        when(tagRepo.findByName(anyString())).thenReturn(Optional.empty());
        when(tagRepo.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepo.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book bookResult = bookService.addBook(mockBook, image);

        assertEquals("Demian", bookResult.getTitle());
        assertEquals("cover123", bookResult.getImageName());
        assertEquals("https://cloudinary/cover123", bookResult.getImageUrl());
        assertEquals("Hesse, Herman", bookResult.getAuthorSort());
        verify(bookRepo).save(mockBook);
    }

    @Test
    void shouldThrowIfBookAlreadyExists() throws IOException {
        Book mockBook = new Book(1,
                "1234567890",
                "Demian",
                "Herman Hesse",
                "",
                true,
                LocalDate.now(),
                "Frankfurt",
                "Verlag",
                32,
                " ",
                " ",
                null
        );
        mockBook.setTags(Set.of(new Tag(1L, "Fantasy")));
        when(bookRepo.findByIsbn("1234567890")).thenReturn(Optional.of(new Book()));

        assertThrows(IllegalArgumentException.class,
                () -> bookService.addBook(mockBook, image));
    }

    @Test
    void shouldUpdateBook() throws IOException {

        Book mockExistingBook = new Book(1,
                "1234567890",
                "Demian",
                "Herman Hesse",
                "Hesse, Herman",
                true,
                LocalDate.now(),
                "Frankfurt",
                "Verlag",
                32,
                "cover123",
                "https://cloudinary/cover123",
                null
        );
        mockExistingBook.setTags(Set.of(new Tag(1L, "Fantasy")));

        Book mockBookUpdate = new Book(1,
                "1234567890",
                "Demian",
                "Hermann Hesse",
                "",
                false,
                LocalDate.now(),
                "Frankfurt am Main",
                "Verlag",
                123,
                " ",
                " ",
                null
        );
        mockBookUpdate.setTags(Set.of(new Tag(1L, "Classic")));

        when(bookRepo.findById(1)).thenReturn(Optional.of(mockExistingBook));
        when(image.getBytes()).thenReturn("fake-image".getBytes());
        Uploader uploaderMock = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploaderMock);
        when(uploaderMock.destroy(anyString(), anyMap())).thenReturn(Map.of("result", "ok"));

        Map<String, Object> uploadResult = Map.of(
                "public_id", "cover456",
                "secure_url", "https://cloudinary/cover456"
        );
        when(uploaderMock.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);
        when(bookRepo.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tagRepo.findByName(anyString())).thenReturn(Optional.empty());
        when(tagRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Book bookResult = bookService.updateBook(1, mockBookUpdate, image);

        assertEquals("Hermann Hesse", bookResult.getAuthor());
        assertEquals(123, bookResult.getPages());
        assertEquals("Hesse, Hermann", bookResult.getAuthorSort());
        assertTrue(bookResult.getTags().stream()
                .anyMatch(tag -> "Classic".equals(tag.getName())));
        assertEquals("cover456", bookResult.getImageName());
        verify(bookRepo).save(mockExistingBook);
    }
}
