package org.example.bookapp.service;

import org.example.bookapp.exception.*;
import org.example.bookapp.model.Author;
import org.example.bookapp.model.Book;
import org.example.bookapp.repository.AuthorRepository;
import org.example.bookapp.repository.BookRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

}
