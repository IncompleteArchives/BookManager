package org.example.bookapp.service;

import org.example.bookapp.exception.*;
import org.example.bookapp.model.Author;
import org.example.bookapp.model.Book;
import org.example.bookapp.repository.AuthorRepository;
import org.example.bookapp.repository.BookRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository repository;
    private final AuthorRepository authorRepository;
    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    @Autowired
    public BookService(BookRepository repository, AuthorRepository authorRepository) {
        this.repository = repository;
        this.authorRepository = authorRepository;
    }

    public Book addBook(String name, Integer publicationYear, Integer authorId) {
        try {

            Book book = new Book(name, publicationYear);

            Author author = authorRepository.findById(authorId).orElseThrow(AuthorNotFoundException::new);

            book.setAuthor(author);

            return repository.save(book);
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
            if (name == null || name.isBlank()) throw new InvalidBookException("Book name cannot be null or blank");

            return repository.findByNameContaining(name);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to find book", e);
        }
    }

    @Transactional
    public void transferBook(Integer bookId, Integer authorId) {
        try {
            Book book = repository.findById(bookId).orElseThrow(BookNotFoundException::new);

            Author author = authorRepository.findById(authorId).orElseThrow(AuthorNotFoundException::new);

            book.setAuthor(author);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to transfer book", e);
        }

    }

    public List<Book> findByNameAndAuthor(String name, Integer authorId) {
        try {
            if (name == null || name.isBlank()) throw new InvalidBookException("Book name cannot be null or blank");
            if (authorId == null) throw new InvalidAuthorException("author ID cannot be null");

            return repository.findByNameContainingAndAuthor_Id(name, authorId);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to find book", e);
        }
    }

    @Transactional
    public void deleteBook(Integer id) {
        try {
            Book book = repository.findById(id).orElseThrow(BookNotFoundException::new);

            repository.delete(book);
        } catch (DataAccessException e) {
            throw new DatabaseOperationException("Unable to delete book", e);
        }
    }

    @Transactional
    public void borrowBook(Integer bookId) {

        log.info("Borrow attempt started: bookId={}", bookId);

        try {
            Book book = repository.findByIdForUpdate(bookId).orElseThrow(() -> {
                log.info("Borrow attempt failed: bookId={}, result=BOOK_NOT_FOUND", bookId);
                return new BookNotFoundException();
            });

            Integer availableCopies = book.getAvailableCopies();

            if (availableCopies > 0) {

                book.setAvailableCopies(availableCopies - 1);

                log.info("Borrow attempt finished: bookId={}, result=SUCCESS, remainingCopies={}",
                        bookId,
                        book.getAvailableCopies()
                );
            } else {
                log.info("Borrow attempt finished: bookId={}, result=NO_AVAILABLE_COPIES", bookId);

                throw new BookNotAvailableException();
            }
        } catch (DataAccessException e) {
            log.error("Borrow attempt failed: bookId={}, result=DATABASE_ERROR", bookId, e);

            throw new DatabaseOperationException("Unable to borrow book", e);
        }

    }

}
