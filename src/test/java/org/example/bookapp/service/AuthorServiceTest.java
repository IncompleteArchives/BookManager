package org.example.bookapp.service;

import org.example.bookapp.exception.AuthorHasBooksException;
import org.example.bookapp.exception.AuthorNotFoundException;
import org.example.bookapp.exception.DatabaseOperationException;
import org.example.bookapp.model.Author;
import org.example.bookapp.model.Book;
import org.example.bookapp.repository.AuthorRepository;
import org.example.bookapp.repository.BookRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @InjectMocks
    AuthorService authorService;

    @Mock
    AuthorRepository authorRepository;

    @Mock
    BookRepository bookRepository;

    @Test
    void findById_shouldReturnAuthor_whenAuthorExists() {
        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        Optional<Author> result = authorService.findById(1);

        assertThat(result).isPresent().contains(author);

        verify(authorRepository).findById(1);
    }

    @Test
    void findById_shouldReturnEmptyOptional_whenAuthorDoesNotExist() {

        when(authorRepository.findById(1)).thenReturn(Optional.empty());

        Optional<Author> result = authorService.findById(1);

        assertThat(result).isEmpty();

        verify(authorRepository).findById(1);
    }

    @Test
    void findById_shouldThrowDatabaseOperationException_whenRepositoryFails() {

        when(authorRepository.findById(1))
                .thenThrow(new DataAccessException("Database error") {});

        assertThatThrownBy(() ->
                authorService.findById(1))
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessage("Unable to retrieve author");

        verify(authorRepository).findById(1);
        verify(bookRepository, never()).findByAuthorId(1);
    }

    @Test
    void deleteAuthor_shouldDeleteAuthor_whenAuthorHasNoBooks() {
        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        when(authorRepository.findById(1)).thenReturn(Optional.of(author));
        when(bookRepository.findByAuthorId(1)).thenReturn(List.of());

        authorService.deleteAuthor(1);

        verify(authorRepository).findById(1);
        verify(bookRepository).findByAuthorId(1);
        verify(authorRepository).delete(1);
    }

    @Test
    void deleteAuthor_shouldThrowAuthorHasBooksException_whenAuthorHasBooks() {

        Book book = new Book("The Haunt", 1999);

        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        when(authorRepository.findById(1)).thenReturn(Optional.of(author));
        when(bookRepository.findByAuthorId(1)).thenReturn(List.of(book));

        assertThatThrownBy(() ->
                authorService.deleteAuthor(1))
                .isInstanceOf(AuthorHasBooksException.class);

        verify(authorRepository).findById(1);
        verify(bookRepository).findByAuthorId(1);
        verify(authorRepository, never()).delete(1);
    }

    @Test
    void deleteAuthor_shouldThrowAuthorNotFoundException_whenAuthorDoesNotExist() {

        when(authorRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authorService.deleteAuthor(1))
                .isInstanceOf(AuthorNotFoundException.class);

        verify(authorRepository).findById(1);
        verify(bookRepository, never()).findByAuthorId(1);
        verify(authorRepository, never()).delete(1);
    }

    @Test
    void deleteAuthor_shouldThrowDatabaseOperationException_whenRepositoryFails() {

        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        when(authorRepository.findById(1)).thenReturn(Optional.of(author));
        when(bookRepository.findByAuthorId(1)).thenReturn(List.of());

        doThrow(new DataAccessException("Database error") {})
                .when(authorRepository)
                .delete(1);

        assertThatThrownBy(() ->
                authorService.deleteAuthor(1))
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessage("Unable to delete author");

        verify(authorRepository).findById(1);
        verify(bookRepository).findByAuthorId(1);
        verify(authorRepository).delete(1);
    }

    @Test
    void addAuthorWithBooks_shouldAddAuthorAndBooks() {

        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        Book book1 = new Book("The Haunt", 1999);
        Book book2 = new Book("Zeph", 1992);
        List<Book> books = List.of(book1, book2);

        when(authorRepository.add(author)).thenReturn(1);

        Integer result = authorService.addAuthorWithBooks(author, books);

        assertThat(result).isEqualTo(1);
        assertThat(book1.getAuthorId()).isEqualTo(1);
        assertThat(book2.getAuthorId()).isEqualTo(1);

        verify(authorRepository).add(author);
        verify(bookRepository).add(book1);
        verify(bookRepository).add(book2);
    }

    @Test
    void addAuthorWithBooks_shouldThrowDatabaseOperationException_whenBookInsertFails() {

        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        Book book1 = new Book("The Haunt", 1999);
        Book book2 = new Book("Zeph", 1992);
        List<Book> books = List.of(book1, book2);

        when(authorRepository.add(author)).thenReturn(5);

        doNothing()
                .when(bookRepository)
                .add(book1);

        doThrow(new DataAccessException("Database error") {})
                .when(bookRepository)
                .add(book2);

        assertThatThrownBy(() ->
                authorService.addAuthorWithBooks(author, books))
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessage("Unable to insert author with books");

        verify(authorRepository).add(author);
        verify(bookRepository).add(book1);
        verify(bookRepository).add(book2);
    }

    @Test
    void addAuthorWithBooks_shouldThrowDatabaseOperationException_whenAuthorInsertFails() {

        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        Book book = new Book("The Haunt", 1999);

        when(authorRepository.add(author)).thenThrow(new DataAccessException("Database error") {});

        assertThatThrownBy(() ->
                authorService.addAuthorWithBooks(author, List.of(book)))
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessage("Unable to insert author with books");

        verify(authorRepository).add(author);
        verify(bookRepository, never()).add(book);
    }

    @Test
    void addAuthor_shouldReturnGeneratedId() {

        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        when(authorRepository.add(author)).thenReturn(1);

        Integer result = authorService.addAuthor(author);

        assertThat(result).isEqualTo(1);

        verify(authorRepository).add(author);
    }

    @Test
    void addAuthor_shouldThrowDatabaseOperationException_whenRepositoryFails() {

        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        when(authorRepository.add(author)).thenThrow(new DataAccessException("Database error") {});

        assertThatThrownBy(() ->
                authorService.addAuthor(author))
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessage("Unable to insert author");

        verify(authorRepository).add(author);
    }

}