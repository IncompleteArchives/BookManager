package org.example.bookapp.service;

import org.example.bookapp.exception.DatabaseOperationException;
import org.example.bookapp.exception.InvalidAuthorException;
import org.example.bookapp.exception.InvalidBookException;
import org.example.bookapp.model.Book;
import org.example.bookapp.repository.AuthorRepository;
import org.example.bookapp.repository.BookRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;

@Service
public class BookService {

    private final BookRepository repository;
    private final AuthorRepository authorRepository;

    @Autowired
    public BookService(BookRepository repository, AuthorRepository authorRepository) {
        this.repository = repository;
        this.authorRepository = authorRepository;
    }

    public void addBook(String name, Integer publicationYear, Integer authorId) {

        try {
            if (name == null || name.isBlank()) {
                throw new InvalidBookException("book name cannot be null or blank");
            }

            int currentYear = Year.now().getValue();
            if (publicationYear == null || publicationYear <= 0 || publicationYear > currentYear) {
                throw new InvalidBookException("publication year must be between 1 and " + currentYear);
            }

            Book book = new Book();
            book.setName(name);
            book.setPublicationYear(publicationYear);
            book.setAuthorId(authorId);

            repository.add(book);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to insert book", e);
        }

    }

    public List<Book> getAllBooks() {
        try {
            return repository.findAll();
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to retrieve books", e);
        }
    }

    public List<Book> findByNameContains(String name) {

        try {
            if (name == null || name.isBlank()) {
                throw new InvalidBookException("Book name cannot be null or blank");
            }

            return repository.findByNameContains(name);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to find book", e);
        }
    }

    public void transferBook(Integer bookId, Integer authorId) {

        try {
            repository.findById(bookId).orElseThrow(() -> new InvalidBookException("book not found"));
            authorRepository.findById(authorId).orElseThrow(() -> new InvalidAuthorException("author not found"));

            repository.updateAuthor(bookId, authorId);

        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to transfer book", e);
        }

    }

}
