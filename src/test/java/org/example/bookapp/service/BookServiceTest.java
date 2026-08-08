package org.example.bookapp.service;

import org.example.bookapp.exception.DatabaseOperationException;
import org.example.bookapp.exception.InvalidAuthorException;
import org.example.bookapp.exception.InvalidBookException;
import org.example.bookapp.model.Author;
import org.example.bookapp.model.Book;
import org.example.bookapp.repository.AuthorRepository;
import org.example.bookapp.repository.BookRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Test
    void addBook_shouldAddBook() {

        String name = "SomeBook";
        Integer publicationYear = 1;
        Integer authorId = 1;

        bookService.addBook(name, publicationYear, authorId);

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);

        verify(bookRepository).add(bookCaptor.capture());

        Book book = bookCaptor.getValue();

        assertThat(book.getName()).isEqualTo(name);
        assertThat(book.getPublicationYear()).isEqualTo(publicationYear);
        assertThat(book.getAuthorId()).isEqualTo(authorId);
    }

    @Test
    void addBook_shouldThrowInvalidBookException_whenNameIsBlank() {

        assertThatThrownBy(() ->
                bookService.addBook("", 2007, 7))
                .isInstanceOf(InvalidBookException.class)
                .hasMessage("book name cannot be null or blank");

        verify(bookRepository, never()).add(any(Book.class));
    }

    @Test
    void addBook_shouldThrowInvalidBookException_whenPublicationYearIsInvalid() {

        int currentYear = Year.now().getValue();

        assertThatThrownBy(() ->
                bookService.addBook("Zeph", currentYear + 1, 7))
                .isInstanceOf(InvalidBookException.class)
                .hasMessage("publication year must be between 1 and " + currentYear);

        verify(bookRepository, never()).add(any(Book.class));
    }

    @Test
    void addBook_shouldThrowDatabaseOperationException_whenRepositoryFails() {

        doThrow(new DataAccessException("Database error") {})
                .when(bookRepository)
                .add(any(Book.class));

        assertThatThrownBy(() -> bookService.addBook("Zeph", 1992, 7))
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessage("Unable to insert book");

        verify(bookRepository).add(any(Book.class));
    }

    @Test
    void getAllBooks_shouldReturnAllBooks() {

        Book book1 = new Book("The Haunt", 1999);
        Book book2 = new Book("Zeph", 1992);
        List<Book> books = List.of(book1, book2);

        when(bookRepository.findAll()).thenReturn(books);

        List<Book> result = bookService.getAllBooks();

        assertThat(result).containsExactly(book1, book2);

        verify(bookRepository).findAll();
    }

    @Test
    void getAllBooks_shouldThrowDatabaseOperationException_whenRepositoryFails() {

        when(bookRepository.findAll()).thenThrow(new DataAccessException("Database error") {});

        assertThatThrownBy(() ->
                bookService.getAllBooks())
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessage("Unable to retrieve books");

        verify(bookRepository).findAll();
    }

    @Test
    void findByNameContains_shouldReturnBooks() {

        Book book1 = new Book("War and Peace", 1867);
        Book book2 = new Book("Peace Like a River", 2001);

        when(bookRepository.findByNameContains("peac")).thenReturn(List.of(book1, book2));

        List<Book> result = bookService.findByNameContains("peac");

        assertThat(result).containsExactly(book1, book2);

        verify(bookRepository).findByNameContains("peac");
    }

    @Test
    void findByNameContains_shouldThrowInvalidBookException_whenNameIsBlank() {

        assertThatThrownBy(() ->
                bookService.findByNameContains(""))
                .isInstanceOf(InvalidBookException.class)
                .hasMessage("Book name cannot be null or blank");

        verify(bookRepository, never()).findByNameContains(anyString());
    }

    @Test
    void findByNameContains_shouldThrowDatabaseOperationException_whenRepositoryFails() {

        when(bookRepository.findByNameContains("peace")).thenThrow(new DataAccessException("Database error") {});

        assertThatThrownBy(() ->
                bookService.findByNameContains("peace"))
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessage("Unable to find book");

        verify(bookRepository).findByNameContains("peace");
    }

    @Test
    void transferBook_shouldTransferBookToAuthor() {

        Book book = new Book("Zeph", 1992);

        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authorRepository.findById(7)).thenReturn(Optional.of(author));

        bookService.transferBook(1, 7);

        verify(bookRepository).findById(1);
        verify(authorRepository).findById(7);
        verify(bookRepository).updateAuthor(1, 7);
    }

    @Test
    void transferBook_shouldThrowInvalidBookException_whenBookNotFound() {

        when(bookRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                bookService.transferBook(1, 7))
                .isInstanceOf(InvalidBookException.class)
                .hasMessage("book not found");

        verify(bookRepository).findById(1);
        verify(authorRepository, never()).findById(5);
        verify(bookRepository, never()).updateAuthor(anyInt(), anyInt());
    }

    @Test
    void transferBook_shouldThrowInvalidAuthorException_whenAuthorNotFound() {

        Book book = new Book("Zeph", 1992);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authorRepository.findById(7)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                bookService.transferBook(1, 7))
                .isInstanceOf(InvalidAuthorException.class)
                .hasMessage("author not found");

        verify(bookRepository).findById(1);
        verify(authorRepository).findById(7);
        verify(bookRepository, never()).updateAuthor(anyInt(), anyInt());
    }

    @Test
    void transferBook_shouldThrowDatabaseOperationException_whenUpdateFails() {

        Book book = new Book("Zeph", 1992);

        Author author = new Author("Audrey",
                null,
                "Barker",
                "F",
                LocalDate.of(1918, 4, 13)
        );

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(authorRepository.findById(7)).thenReturn(Optional.of(author));

        doThrow(new DataAccessException("Database error") {})
                .when(bookRepository)
                .updateAuthor(1, 7);

        assertThatThrownBy(() ->
                bookService.transferBook(1, 7))
                .isInstanceOf(DatabaseOperationException.class)
                .hasMessage("Unable to transfer book");

        verify(bookRepository).findById(1);
        verify(authorRepository).findById(7);
        verify(bookRepository).updateAuthor(1, 7);
    }


}