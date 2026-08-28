package org.example.bookapp.service;

import org.example.bookapp.exception.AuthorHasBooksException;
import org.example.bookapp.exception.AuthorNotFoundException;
import org.example.bookapp.exception.DatabaseOperationException;
import org.example.bookapp.model.Author;
import org.example.bookapp.model.Book;
import org.example.bookapp.repository.AuthorRepository;

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

    @Test
    void updateAuthor_shouldUpdateAuthor_withoutSave() {

        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        Author authorData = new Author("Jane",
                "Marie",
                "Smith",
                "F",
                LocalDate.of(1920, 5, 20)
        );

        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        authorService.updateAuthor(1, authorData);

        assertThat(author.getFirstName()).isEqualTo("Jane");
        assertThat(author.getMiddleName()).isEqualTo("Marie");
        assertThat(author.getLastName()).isEqualTo("Smith");
        assertThat(author.getGender()).isEqualTo("F");
        assertThat(author.getBirthDate()).isEqualTo(LocalDate.of(1920, 5, 20));

        verify(authorRepository).findById(1);
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    void updateAuthor_shouldThrowAuthorNotFoundException_whenAuthorDoesNotExist() {

        Author authorData = new Author("Jane",
                "Marie",
                "Smith",
                "F",
                LocalDate.of(1920, 5, 20)
        );

        when(authorRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authorService.updateAuthor(1, authorData))
                .isInstanceOf(AuthorNotFoundException.class);

        verify(authorRepository).findById(1);
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    void updateAuthor_shouldThrowDatabaseOperationException_whenRepositoryFails() {

        Author authorData = new Author("Jane",
                "Marie",
                "Smith",
                "F",
                LocalDate.of(1920, 5, 20)
        );

        when(authorRepository.findById(1)).thenThrow(new DataAccessException("Database error") {});

        assertThatThrownBy(() ->
                authorService.updateAuthor(1, authorData))
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessage("Unable to update author");

        verify(authorRepository).findById(1);
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    void findById_shouldReturnAuthor_whenAuthorExists() {
        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        when(authorRepository.findByIdWithBooks(1)).thenReturn(Optional.of(author));

        Optional<Author> result = authorService.findById(1);

        assertThat(result).isPresent().contains(author);

        verify(authorRepository).findByIdWithBooks(1);
    }

    @Test
    void findById_shouldReturnEmptyOptional_whenAuthorDoesNotExist() {

        when(authorRepository.findByIdWithBooks(1)).thenReturn(Optional.empty());

        Optional<Author> result = authorService.findById(1);

        assertThat(result).isEmpty();

        verify(authorRepository).findByIdWithBooks(1);
    }

    @Test
    void findById_shouldThrowDatabaseOperationException_whenRepositoryFails() {

        when(authorRepository.findByIdWithBooks(1))
                .thenThrow(new DataAccessException("Database error") {
                });

        assertThatThrownBy(() ->
                authorService.findById(1))
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessage("Unable to retrieve author");

        verify(authorRepository).findByIdWithBooks(1);
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

        authorService.deleteAuthor(1);

        verify(authorRepository).findById(1);
        verify(authorRepository).delete(author);
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
        author.addBook(book);

        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        assertThatThrownBy(() ->
                authorService.deleteAuthor(1))
                .isInstanceOf(AuthorHasBooksException.class);

        verify(authorRepository).findById(1);
        verify(authorRepository, never()).delete(author);
    }

    @Test
    void deleteAuthor_shouldThrowAuthorNotFoundException_whenAuthorDoesNotExist() {

        when(authorRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authorService.deleteAuthor(1))
                .isInstanceOf(AuthorNotFoundException.class);

        verify(authorRepository).findById(1);
        verify(authorRepository, never()).delete(any(Author.class));
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

        doThrow(new DataAccessException("Database error") {})
                .when(authorRepository)
                .delete(author);

        assertThatThrownBy(() ->
                authorService.deleteAuthor(1))
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessage("Unable to delete author");

        verify(authorRepository).findById(1);
        verify(authorRepository).delete(author);
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

        when(authorRepository.save(author)).thenAnswer(
                invocation -> {
                    author.setId(1);
                    return author;
                });

        Integer result = authorService.addAuthorWithBooks(author, books);

        assertThat(result).isEqualTo(1);
        assertThat(author.getBooks()).containsExactly(book1, book2);
        assertThat(book1.getAuthor()).isSameAs(author);
        assertThat(book2.getAuthor()).isSameAs(author);

        verify(authorRepository).save(author);
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

        when(authorRepository.save(author)).thenThrow(new DataAccessException("Database error") {});

        assertThatThrownBy(() ->
                authorService.addAuthorWithBooks(author, books))
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessage("Unable to insert author with books");

        verify(authorRepository).save(author);
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

        when(authorRepository.save(author)).thenThrow(new DataAccessException("Database error") {});

        assertThatThrownBy(() ->
                authorService.addAuthorWithBooks(author, List.of(book)))
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessage("Unable to insert author with books");

        verify(authorRepository).save(author);
    }

    @Test
    void addAuthor_shouldReturnGeneratedId() {

        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        author.setId(1);

        when(authorRepository.save(author)).thenReturn(author);

        Integer result = authorService.addAuthor(author).getId();

        assertThat(result).isEqualTo(1);

        verify(authorRepository).save(author);
    }

    @Test
    void addAuthor_shouldThrowDatabaseOperationException_whenRepositoryFails() {

        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        when(authorRepository.save(author)).thenThrow(new DataAccessException("Database error") {});

        assertThatThrownBy(() ->
                authorService.addAuthor(author))
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessage("Unable to insert author");

        verify(authorRepository).save(author);
    }

}